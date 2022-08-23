package service.ai

import entity.*
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.BackpropType
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.learning.config.Adam
import service.GameService
import service.RootService
import service.constructGraph
import service.destinationPool
import view.Refreshable
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


class QLearning {
    val inputArray: INDArray = Nd4j.zeros(158,3)
    var outputArray: INDArray = Nd4j.zeros(122)
    val random = java.util.Random()

    private val routes = mutableSetOf<Route>()
    init {
        constructGraph().forEach {
            routes.addAll(it.routes)
        }
    }

    /**
     * Compute the cards required to pay the tunnel fee
     */
    private fun State.monteCarloPayTunnel(route: Tunnel, used: List<WagonCard>): List<WagonCard>? {
        val required = wagonCardsStack.run { subList(max(0, size - 3), size) }
        //if only locomotive cards have been used, only locomotive cards have to be payed
        return if (used.all { it.color == Color.JOKER }) {
            val requiredCount = required.count { it.color == Color.JOKER }
            val usable = currentPlayer.wagonCards.filter { it.color == Color.JOKER }
            if (usable.size < requiredCount) null else usable.take(requiredCount)
        } else {
            val requiredCount = required.count { it.color == Color.JOKER || it.color == route.color }
            val usable =
                currentPlayer.wagonCards.filter { it.color == Color.JOKER || it.color == route.color }
            if (usable.size < requiredCount) null else usable.shuffled().take(requiredCount)
        }
    }

    private fun RootService.executeAiMove(move: AIMove) {
        when (move) {
            is AIMove.ClaimRoute -> {
                playerActionService.claimRoute(move.route, move.usedCards)
                if (game.gameState == GameState.AFTER_CLAIM_TUNNEL) {
                    // sometimes the required cards needed to pay for the tunnel are specific to the
                    // order the shuffled discard stack, which means that the cards used to pay
                    // may work for one shuffle order of the discard stack but not for another
                    // which means we have re-calculate the cards required
                    try {
                        playerActionService.afterClaimTunnel(move.route as Tunnel, move.tunnelCards)
                    } catch (_: IllegalStateException) {
                        playerActionService.afterClaimTunnel(
                            move.route as Tunnel,
                            game.currentState.monteCarloPayTunnel(move.route, move.usedCards)
                        )
                    }
                }
            }

            is AIMove.DrawDestinationCard -> {
                playerActionService.drawDestinationCards(move.destinationCards.toList())
            }

            is AIMove.DrawWagonCard -> {
                playerActionService.drawWagonCard(move.firstDraw)
                playerActionService.drawWagonCard(move.secondDraw)
            }
        }
    }

    private class WinnerReporter(var winner: Player? = null) : Refreshable {
        override fun refreshAfterEndGame(winner: Player) {
            this.winner = winner
        }

        fun report(): Player? = winner.also { winner = null }
    }

    private sealed interface RandomMove {
        data class DrawWagonCard(val firstDraw: Int, val secondDraw: Int) : RandomMove
        object DrawDestinationCard : RandomMove
        data class ClaimRoute(val route: Route) : RandomMove
    }

    private fun State.getPossibleMoves(): List<RandomMove> {
        val drawWagonCards = if (wagonCardsStack.size + discardStack.size>= 2) {
            (0..5).flatMap { m1 -> (0..5).map { m2 -> RandomMove.DrawWagonCard(m1, m2) } }
        } else {
            emptyList()
        }
        val routes = IdentityHashMap<Route, Unit>(79)
        cities.flatMap { it.routes }.forEach { routes[it] = Unit }
        val exploreRoot = RootService().apply { game = Game(this@getPossibleMoves) }
        val validRoutes = routes.keys.filter {
            kotlin.runCatching {
                exploreRoot.playerActionService.validateClaimRoute(currentPlayer, it, currentPlayer.wagonCards, false)
            }.isSuccess
        }.map(RandomMove::ClaimRoute)
        val destinationDrawAction = if (destinationCards.isNotEmpty()) {
            listOf(RandomMove.DrawDestinationCard)
        } else {
            emptyList()
        }
        return drawWagonCards + validRoutes + destinationDrawAction
    }

    fun actionLegal(action: Int, rootService: RootService): Boolean{
        val root = RootService().apply { game=Game(rootService.game.currentState) }
        if (action !in 0..121) return false
        val state = root.game.currentState
        val allowedMoves = state.getPossibleMoves()
        val aimove = try{toAiMove(action, root)} catch (e: Exception){return false} catch (e: java.lang.AssertionError){return false}
        try {root.executeAiMove(aimove)} catch (e: Exception){return false}
        return true
//        when (aimove){
//            is AIMove.DrawDestinationCard -> {return root.game.currentState.destinationCards.size >= 3}
//            is AIMove.DrawWagonCard -> {
//               try {root.executeAiMove(aimove)} catch (e: Exception){return false}
//                return true
//             }
//            is AIMove.ClaimRoute ->  {
//                if(root.game.currentState.currentPlayer.wagonCards.size < aimove.route.completeLength || aimove.route is Ferry) return false
//                return allowedMoves.any { it == RandomMove.ClaimRoute(aimove.route) }
//            }
//        }
    }

    private fun getClaimRouteAIMove(route: Route, root: RootService): AIMove.ClaimRoute {
        val state = root.game.currentState
        when (route) {
            is Ferry -> {
                val currentPlayer = state.currentPlayer
                val cards = mutableListOf<WagonCard>()
                cards += currentPlayer.wagonCards.filter { it.color == Color.JOKER }.run {
                    takeLast(min(route.completeLength, size))
                }
                val addedFerries = cards.size
                val counts = currentPlayer.wagonCards.groupBy { it.color }.mapValues { it.value.count() }.toMutableMap()
                counts[Color.JOKER] = 0
                val selectedColor: Color = if (route.color == Color.JOKER) {
                    counts.maxByOrNull { it.value }?.key ?: Color.BLUE
                } else {
                    route.color
                }
                val coloredCardsRequired = route.length + min(route.ferries - cards.size, 0)
                currentPlayer.wagonCards.forEach {
                    if ((cards.size - addedFerries) < coloredCardsRequired && it.color == selectedColor) {
                        cards.add(it)
                    }
                }
                if (cards.size < route.completeLength) {
                    val missingCards = route.completeLength - cards.size
                    cards += currentPlayer.wagonCards.filter { cards.none { card -> card === it } }.shuffled()
                        .takeLast(3 * missingCards)
                }
                return AIMove.ClaimRoute(route, cards, null)
            }

            is Tunnel -> {
                val used = state.currentPlayer.wagonCards
                    .filter { it.color == route.color || it.color == Color.JOKER }
                    .shuffled().take(route.length)

                root.playerActionService.claimRoute(route, used)
                if (root.game.gameState != GameState.AFTER_CLAIM_TUNNEL){
                    root.undo()
                    return AIMove.ClaimRoute(route, used, null)
                }
                if (Random.nextInt(0, 4) == 0) {
                    root.undo()
                    return AIMove.ClaimRoute(route, used, null)
                }
                val state = root.game.currentState
                val required = state.wagonCardsStack.run { subList(max(0, state.wagonCardsStack.size - 3), size) }
                val used2 = if (used.all { it.color == Color.JOKER }) {
                    val requiredCount = required.count { it.color == Color.JOKER }
                    val usable = state.currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                    if (usable.size < requiredCount) null else usable.take(requiredCount)
                } else {
                    val requiredCount = required.count { it.color == Color.JOKER || it.color == route.color }
                    val usable =
                        state.currentPlayer.wagonCards.filter { it.color == Color.JOKER || it.color == route.color }
                    if (usable.size < requiredCount) null else usable.shuffled().take(requiredCount)
                }
                root.undo()
                return AIMove.ClaimRoute(route, used, used2)
            }

            else -> {
                if (route.color != Color.JOKER) {
                    val cards = state.currentPlayer.wagonCards
                        .filter { it.color == route.color }
                        .take(route.length)
                    return AIMove.ClaimRoute(route, cards, null)
                } else {
                    val counts = state.currentPlayer.wagonCards
                        .groupBy { it.color }.mapValues { it.value.count() }
                    val maxFittingCount = counts.filterValues { it >= route.length }
                        .toList().randomOrNull()
                    if (maxFittingCount != null) {
                        val cards = state.currentPlayer.wagonCards
                            .filter { it.color == maxFittingCount.first }
                            .take(route.length)
                        return AIMove.ClaimRoute(route, cards, null)
                    } else if (route.isMurmanskLieksa()) {
                        val maxCount = counts.maxByOrNull { it.value }
                        checkNotNull(maxCount)
                        val required = 9 - maxCount.value
                        val used = state.currentPlayer.wagonCards.filter { it.color == maxCount.key }.toMutableList()
                        val left = state.currentPlayer.wagonCards.filter { it.color != maxCount.key }.shuffled()
                        used.addAll(left.subList(0, required * 4))
                        return AIMove.ClaimRoute(route, used, null)
                    } else {
                        throw AssertionError("Invalid claim")
                    }
                }
            }
        }
    }

    fun doStuff(map: MutableMap<Color, Int>, wg: WagonCard, stateArray: INDArray, layer: Int){
        val index = map[wg.color] ?: throw IllegalStateException()
        val arrIndex = IntArray(2).apply {
            set(0, index)
            set(1, layer)
        }
        stateArray.putScalar(arrIndex, 1)
        map[wg.color] = index + 1
    }

    val destCards = destinationPool(constructGraph().associateBy { it.name })

    fun readState(state:State, stateArray: INDArray){
        var map = Color.values().associateWith { it.ordinal*12 }.toMutableMap()
        val destCardStart = map[Color.JOKER]?.plus(14) ?: throw IllegalStateException()
       // layer 0: nachziehkarten und destinationCards
        state.openCards.forEach{
            doStuff(map, it, stateArray, 0)
       }
        state.wagonCardsStack.forEach{
            doStuff(map, it, stateArray, 0)
        }
        state.destinationCards.forEach{
            val i = destCards.indexOfFirst {dCard -> it == dCard }
            val arrIndex = IntArray(2).apply {
                set(0, destCardStart+i)
                set(1, 0)
            }
            stateArray.putScalar(arrIndex, 1)
        }
        // zustand der routen layer 1 - 2
        val routes = mutableSetOf<Route>()
        state.cities.forEach {
            routes.addAll(it.routes)
        }
        routes.forEachIndexed{rIndex, r ->
            state.players.forEachIndexed { pIndex, p ->
                    val arrIndex = IntArray(2).apply {
                        set(0, rIndex)
                        set(1, pIndex + 1)
                    }
                    val value = if (r in p.claimedRoutes) 1 else 0
                    stateArray.putScalar(arrIndex, value)
            }
        }
        state.players.forEachIndexed { index, player ->
            val interval = NDArrayIndex.interval(routes.size, routes.size + player.trainCarsAmount + 1)
            stateArray.getColumn((index+1).toLong()).get(interval).assign(Nd4j.ones(interval.length()))
        }
    }

    fun toAiMove(action: Int, rootService: RootService): AIMove{
        val exploreRoot = RootService().apply { game=Game(rootService.game.currentState) }
        val state = exploreRoot.game.currentState
        val routes = mutableSetOf<Route>()
        state.cities.forEach {
            routes.addAll(it.routes)
        }
        when (action) {
            in 0 until 36 -> {
                val first = action / 6
                val second = action % 6
                return AIMove.DrawWagonCard(first, second)
            }
            in 36 until 43 -> {
                val cards = mutableListOf<Int>()
                when (action - 36){
                    6 -> {cards.addAll(listOf(0,1,2))}
                    in 3 until 6 -> {cards.add(action-39)}
                    in 1 until 3 -> {
                        cards.add((action-36)/3)
                        cards.add((action-36)%3)
                    }
                    0 -> {cards.add(1); cards.add(2)}
                }
                return AIMove.DrawDestinationCard(cards)
            }
            in 43 until 122 -> {
                return getClaimRouteAIMove(routes.elementAt(action-43), exploreRoot)
            }
        }
        throw IllegalStateException("action not in valid range 0-121")
    }

    fun getModel(): MultiLayerConfiguration {
        return NeuralNetConfiguration.Builder()
            .weightInit(WeightInit.XAVIER)
            .activation(Activation.SOFTMAX)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(Adam(0.05))
            .list()
            .layer(0,DenseLayer.Builder().nIn(158*3).nOut(300).build())
            .layer(1,DenseLayer.Builder().nIn(300).nOut(300).build())
            .layer(2,DenseLayer.Builder().nIn(300).nOut(300).build())
            .layer(3,DenseLayer.Builder().nIn(300).nOut(300).build())
            .layer(4,DenseLayer.Builder().nIn(300).nOut(300).build())
            .layer(5,DenseLayer.Builder().nIn(300).nOut(122).build())
            .layer(6, OutputLayer.Builder().nIn(122).nOut(122).build())
            .backpropType(BackpropType.Standard)
            .build()
    }

    fun learn(numEpisodes: Int, f: File? = null, self: Boolean = false){
        val model = if (f != null){
            MultiLayerNetwork.load(f, true)
        } else{
            val conf = getModel()
            MultiLayerNetwork(conf)
        }

        model.init()
        val discount_factor = 1
        var eps = 0.5
        val eps_decay_factor = 0.999
        repeat(numEpisodes){
            val winnerReporter = WinnerReporter()
            val rootService = RootService()
            rootService.gameService.addRefreshable(winnerReporter)
            eps *= eps_decay_factor
            rootService.gameService.startNewGame(listOf(
                GameService.PlayerData("neuralNetwork", false),
                GameService.PlayerData("randy", false)
            ))
            rootService.gameService.chooseDestinationCard(List(3) { (0..4).toList() })
            println("iteration:$it")
            while (winnerReporter.report() == null){
                var flag = false
                var action = -1
                var lastAction = action
                if (random.nextFloat() < eps){
                    while (!actionLegal(action, rootService)){
                        action = random.nextInt(122)
                    }

                } else {

                    readState(rootService.game.currentState, inputArray)
                    outputArray = model.output(inputArray.reshape(1,158*3))
                    while (!actionLegal(action, rootService)){
                        if(action >= 0){
                            outputArray.putScalar(action.toLong(), -100)
                        }
                        action = outputArray.argMax().toString().toInt()
                        if (lastAction == action){
                            print("same")
                            flag = true
                            break
                        }
                        lastAction = action
                    }
                    if (flag)
                        break
                }
                if (flag)
                    break
                val aimove = toAiMove(action, rootService)
                rootService.executeAiMove(aimove)
                if (self)
                    nextAiMove(rootService)
                else
                    rootService.randomNextTurn()
                var reward = 0.0
                reward = if (winnerReporter.winner?.name == "neuralNetwork"){
                    println("reward1")
                    1.0
                } else if (winnerReporter.winner == null){
                    0.5
                } else {
                    println("reward-1")
                    0.0
                }
                val target = reward + discount_factor * model.output(inputArray.reshape(1,158*3)).max(0).getDouble(0)
                val targetVector = model.output(inputArray.reshape(1,158*3))
                targetVector.putScalar(action.toLong(), target)
                model.fit(inputArray.reshape(1,158*3), targetVector)

            }
        }
        model.save(File("model.h5"))
    }

    private val playModel = MultiLayerNetwork.load(File("model-gut.h5"), true)

    fun nextAiMove(root: RootService){
        readState(root.game.currentState, inputArray)
        outputArray = playModel.output(inputArray.reshape(1,158*3))
        var action = -1
        while (!actionLegal(action, root)){
            if(action >= 0){
                outputArray.putScalar(action.toLong(), -100)
            }
            action = outputArray.argMax().toString().toInt()
        }
        val aiMove = toAiMove(action, root)
        root.executeAiMove(aiMove)
    }

}