package service.ai

import entity.*
import service.GameService
import service.RootService
import view.Refreshable
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random.Default.nextInt

class AIService(private val root: RootService) {
    sealed interface AIMove {
        data class DrawWagonCard(val firstDraw: Int, val secondDraw: Int) : AIMove
        object DrawDestinationCard : AIMove
        data class ClaimRoute(val route: Route) : AIMove
    }

    val state: State
        get() = root.game.currentState


    sealed interface Move {
        data class DrawWagonCard(val firstDraw: Int, val secondDraw: Int) : Move
        data class DrawDestinationCard(val destinationCards: List<Int>) : Move
        data class ClaimRoute(val route: Route, val usedCards: List<WagonCard>, val tunnelCards: List<WagonCard>?) :
            Move
    }

    class DecisionTree(val parent: DecisionTree?, val value: State, val move: Move, val level: Int, val score: Int) {
        val children: MutableList<DecisionTree> = mutableListOf()
        fun isLeaf(): Boolean {
            return children.isEmpty()
        }
    }

    fun getPossibleMoves(_state: State): List<AIMove> {
        val drawWagonCards = if (_state.run { wagonCardsStack.size + discardStack.size } >= 2) {
            (0..5).flatMap { m1 -> (0..5).map { m2 -> AIMove.DrawWagonCard(m1, m2) } }
        } else {
            emptyList()
        }
        val routes = IdentityHashMap<Route, Unit>(79)
        _state.cities.flatMap { it.routes }.forEach { routes[it] = Unit }
        val currentPlayer = root.game.currentState.currentPlayer
        val validRoutes = routes.keys.filter {
            kotlin.runCatching {
                root.playerActionService.validateClaimRoute(currentPlayer, it, currentPlayer.wagonCards, false)
            }.isSuccess
        }.map(AIMove::ClaimRoute)
        val destinationDrawAction = if (state.destinationCards.isNotEmpty()) {
            listOf(AIMove.DrawDestinationCard)
        } else {
            emptyList()
        }
        return drawWagonCards + validRoutes + destinationDrawAction
    }

    /*fun goodNextTurn(){
        val _root = RootService()
        _root.game = Game(root.game.currentState)
        val possibleMoves = getPossibleMoves(_root.game.currentState)
        for (move in possibleMoves) {
            when (move){
                is AIMove.DrawWagonCard -> {}
                is AIMove.ClaimRoute -> { aiClaimRoute(move.route, _root.game.currentState, _route) }
                is AIMove.DrawDestinationCard -> {}
            }
        }
    }*/

    fun randomNextTurn() {
//        val drawWagonCards = if (state.run { wagonCardsStack.size + discardStack.size } >= 2) {
//            (0..5).flatMap { m1 -> (0..5).map { m2 -> AIMove.DrawWagonCard(m1, m2) } }
//        } else {
//            emptyList()
//        }
//        val routes = IdentityHashMap<Route, Unit>(79)
//        state.cities.flatMap { it.routes }.forEach { routes[it] = Unit }
//        val currentPlayer = root.game.currentState.currentPlayer
//        val validRoutes = routes.keys.filter {
//            kotlin.runCatching {
//                root.playerActionService.validateClaimRoute(currentPlayer, it, currentPlayer.wagonCards, false)
//            }.isSuccess
//        }.map(AIMove::ClaimRoute)
//        val destinationDrawAction = if (state.destinationCards.isNotEmpty()) {
//            listOf(AIMove.DrawDestinationCard)
//        } else {
//            emptyList()
//        }
//        val actionSpace = drawWagonCards + validRoutes + destinationDrawAction
        val actionSpace = getPossibleMoves(state)
        val action = actionSpace.random()
        when (action) {
            is AIMove.ClaimRoute -> {
                randomClaimRoute(action.route)
            }

            AIMove.DrawDestinationCard -> {
                randomDrawDestinationCard()
            }

            is AIMove.DrawWagonCard -> {
                root.playerActionService.run {
                    drawWagonCard(action.firstDraw)
                    drawWagonCard(action.secondDraw)
                }
            }
        }
    }

    private fun randomDrawDestinationCard() {
        val valid = (0 until min(state.destinationCards.size, 3)).toMutableList().apply { shuffle() }
        val removeIndices = if (valid.size == 1) 0 else nextInt(0, valid.size - 1)
        root.playerActionService.drawDestinationCards(valid.subList(0, valid.size - removeIndices))
    }

    private fun randomClaimRoute(route: Route) {
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
                root.playerActionService.claimRoute(route, cards)
            }

            is Tunnel -> {
                val used = state.currentPlayer.wagonCards
                    .filter { it.color == route.color || it.color == Color.JOKER }
                    .shuffled().take(route.length)
                root.playerActionService.claimRoute(route, used)
                if (root.game.gameState != GameState.AFTER_CLAIM_TUNNEL) return
                if (nextInt(0, 4) == 0) {
                    root.playerActionService.afterClaimTunnel(route, null)
                    return
                }
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
                root.playerActionService.afterClaimTunnel(route, used2)
            }

            else -> {
                if (route.color != Color.JOKER) {
                    val cards = state.currentPlayer.wagonCards
                        .filter { it.color == route.color }
                        .take(route.length)
                    root.playerActionService.claimRoute(route, cards)
                } else {
                    val counts = root.game.currentState.currentPlayer.wagonCards
                        .groupBy { it.color }.mapValues { it.value.count() }
                    val maxFittingCount = counts.filterValues { it >= route.length }
                        .toList().randomOrNull()
                    if (maxFittingCount != null) {
                        val cards = root.game.currentState.currentPlayer.wagonCards
                            .filter { it.color == maxFittingCount.first }
                            .take(route.length)
                        root.playerActionService.claimRoute(route, cards)
                    } else if (route.isMurmanskLieksa()) {
                        val maxCount = counts.maxByOrNull { it.value }
                        checkNotNull(maxCount)
                        val required = 9 - maxCount.value
                        val used = state.currentPlayer.wagonCards.filter { it.color == maxCount.key }.toMutableList()
                        val left = state.currentPlayer.wagonCards.filter { it.color != maxCount.key }.shuffled()
                        used.addAll(left.subList(0, required * 4))
                        root.playerActionService.claimRoute(route, used)
                    } else {
                        throw AssertionError("Invalid claim")
                    }
                }
            }
        }
    }

    companion object {
        fun randomAIGame() {
            playGame(AIService::randomNextTurn)
        }
        fun minMaxAIGame() {
            playGame(AIService::minMaxMove)
        }

        fun playGame(ai: AIService.() -> Unit) {
            val root = RootService()
            root.gameService.startNewGame(
                listOf(
                    GameService.PlayerData("dnaida", false),
                    GameService.PlayerData("dnaidasd", false),
                    GameService.PlayerData("asdsasda", false)
                )
            )
            root.gameService.chooseDestinationCard(List(3) { (0..4).toList() })
            val refreshable = object : Refreshable {
                var ended = false
                override fun refreshAfterEndGame(winner: Player) {
                    ended = true
                }
            }
            root.addRefreshable(refreshable)
            val aiService = AIService(root)
            while (!refreshable.ended) {
                aiService.ai()
            }
        }
    }

    fun monteCarloMove() {
        root.monteCarloMove()
    }

    fun minMaxMove() {
        val playerIndex = state.currentPlayerIndex
        val unclaimedRoutes = run {
            val routes = IdentityHashMap<Route, Unit>(79)
            root.game.currentState.cities.flatMap { it.routes }.forEach { routes[it] = Unit }
            val doubleClaimAllowed = root.game.currentState.players.size > 2
            root.game.currentState.players.flatMap { it.claimedRoutes }.forEach {
                routes.remove(it)
                if (it.sibling != null && !doubleClaimAllowed) {
                    routes.remove(it.sibling)
                }
            }
            routes.keys.toList()
        }
        var bestRoute: DecisionTree? = null
        val moves = LinkedBlockingQueue<DecisionTree>()
        computeMinMaxMoves(unclaimedRoutes, null, state) { move, state ->
            moves.add(DecisionTree(null, state, move, 0, computeScore(state, state.players[playerIndex])))
        }
        val start = Instant.now()
        for (i in 0..50000) {
            val node = moves.poll()
            var bestChild: DecisionTree? = null
            computeMinMaxMoves(unclaimedRoutes, node, node.value) { move, state ->
                val score = computeScore(state, state.players[node.value.currentPlayerIndex])
                val newChild = DecisionTree(node, state, move, level = node.level + 1, score)
                moves.add(newChild)
                if ((bestChild?.score ?: 0) < score) {
                    bestChild = newChild
                }
            }

            val malus = node.parent?.let {
                it.score + if (state.players.size > 2 && it.parent != null) it.parent.score else 0
            } ?: 0
            val bestRouteNow = bestRoute
            val bestChildNow = bestChild
            if (bestChildNow != null && bestChildNow.value.currentPlayerIndex == state.currentPlayerIndex) {
                if (bestRouteNow == null ||
                    (bestRouteNow.score < (bestChildNow.score - malus)) ||
                    (bestRouteNow.level < (bestChildNow.level))
                ) {
                    bestRoute = bestChild
                }
            }
        }
        val time = Duration.between(start, Instant.now())
        println(time.toMillis())
        checkNotNull(bestRoute)
        println("${bestRoute.level / 3}")
        var head: DecisionTree = bestRoute
        while (head.parent != null) {
            head = head.parent ?: break
        }
        root.executeMove(head.move)
    }
}

fun RootService.executeMove(move: AIService.Move) {
    when(move) {
        is AIService.Move.ClaimRoute -> {
            playerActionService.claimRoute(move.route, move.usedCards.toList())
            if (game.gameState == GameState.AFTER_CLAIM_TUNNEL) {
                playerActionService.afterClaimTunnel(move.route as Tunnel, move.tunnelCards)
            }
        }
        is AIService.Move.DrawDestinationCard -> {
            playerActionService.drawDestinationCards(move.destinationCards.toList())
        }
        is AIService.Move.DrawWagonCard -> {
            playerActionService.drawWagonCard(move.firstDraw)
            playerActionService.drawWagonCard(move.secondDraw)
        }
    }
}

private inline fun computeMinMaxMoves(
    claimableRoute: List<Route>,
    parent: AIService.DecisionTree?,
    state: State,
    emit: (AIService.Move, State) -> Unit
) {
    val root = RootService().apply { game = Game(state) }
    allClaimableRoutes(claimableRoute, parent, root) {
        minMaxConvertAIMoveToMove(RootService().apply { game = Game(state) }, AIService.AIMove.ClaimRoute(it), emit)
    }
    withAllMinMaxMoves(root) {
        minMaxConvertAIMoveToMove(RootService().apply { game = Game(state) }, it, emit)
    }
}

private inline fun withAllMinMaxMoves(exploreRoot: RootService, with: (AIService.AIMove) -> Unit) {
    val state = exploreRoot.game.currentState
    val drawWagonCards = uniqueDrawWagonCard(exploreRoot)
    val destinationDrawAction = if (state.destinationCards.isNotEmpty()) {
        listOf(AIService.AIMove.DrawDestinationCard)
    } else {
        emptyList()
    }
    val validMoves = drawWagonCards + destinationDrawAction
    validMoves.forEach(with)
}
private inline fun minMaxConvertAIMoveToMove(exploreRoot: RootService, move: AIService.AIMove, emit: (AIService.Move, State) -> Unit) {
    when (move) {
        is AIService.AIMove.ClaimRoute -> aiClaimRoute(move.route, exploreRoot, emit)
        AIService.AIMove.DrawDestinationCard -> {
            minMaxDrawDestinationCard(exploreRoot, emit)
        }
        is AIService.AIMove.DrawWagonCard -> {
            exploreRoot.playerActionService.drawWagonCard(move.firstDraw)
            exploreRoot.playerActionService.drawWagonCard(move.secondDraw)
            emit(AIService.Move.DrawWagonCard(move.firstDraw, move.secondDraw), exploreRoot.game.currentState)
        }
    }
}

private fun computeScore(state: State, player: Player): Int {
    //TODO
    return player.points
}

private val wagonCardMoves = ArrayList<AIService.AIMove.DrawWagonCard>(3).apply {
    add(AIService.AIMove.DrawWagonCard(5, 5))
    for (i in 0..4) {
        add(AIService.AIMove.DrawWagonCard(i, 5))
        add(AIService.AIMove.DrawWagonCard(5, i))
        for (j in i..4) {
            add(AIService.AIMove.DrawWagonCard(i, j))
        }
    }
}

inline fun allClaimableRoutes(
    claimableRoute: List<Route>,
    node: AIService.DecisionTree?,
    exploreRoot: RootService,
    with: (Route) -> Unit
) {
    val currentPlayer = exploreRoot.game.currentState.currentPlayer
    val allowClaimDouble = exploreRoot.game.currentState.players.size > 2
    for (route in claimableRoute) {
        var canClaim = true
        var head: AIService.DecisionTree? = node
        while (head != null) {
            val move = head.move
            if (move is AIService.Move.ClaimRoute) {
                if (move.route === route) {
                    canClaim = false
                    break
                }
                if (!allowClaimDouble && move.route.sibling === route) {
                    canClaim = false
                    break
                }
            }
            head = head.parent
        }
        if (route.sibling != null && currentPlayer.claimedRoutes.any { it === route.sibling }) {
            continue
        }
        if (!canClaim) continue
        if (currentPlayer.trainCarsAmount < route.completeLength) continue
        if (!exploreRoot.playerActionService.canClaimRoute(route, currentPlayer.wagonCards, false)) continue
        with(route)
    }
}

fun uniqueDrawWagonCard(exploreRoot: RootService): List<AIService.AIMove.DrawWagonCard> {
    val canDraw = exploreRoot.game.currentState.run { wagonCardsStack.size + discardStack.size } >= 2
    val drawWagonCards = if (canDraw) {
        wagonCardMoves
    } else {
        return emptyList()
    }
    val moves: HashMap<Long, AIService.AIMove.DrawWagonCard> = HashMap(20)
    val countArray = IntArray(9) { 0 }
    for (move in drawWagonCards) {
        val currentPlayerIndex = exploreRoot.game.currentState.currentPlayerIndex
        exploreRoot.playerActionService.drawWagonCard(move.firstDraw)
        exploreRoot.playerActionService.drawWagonCard(move.secondDraw)
        for (card in exploreRoot.game.currentState.openCards) {
            countArray[card.color.ordinal] += 1
        }
        var hash: Long = 0
        var factor: Long = 1
        for (i in 0 until 9) {
            hash += countArray[i] * factor
            factor *= 9
            countArray[i] = 0
        }
        val oldPlayer = exploreRoot.game.currentState.players[currentPlayerIndex]
        var firstCard = oldPlayer.wagonCards[oldPlayer.wagonCards.size - 1].color
        var secondCard = oldPlayer.wagonCards[oldPlayer.wagonCards.size - 2].color
        if (firstCard.ordinal < secondCard.ordinal) {
            val tmp = firstCard
            firstCard = secondCard
            secondCard = tmp
        }
        hash = hash.shl(7).or((firstCard.ordinal * 9 + secondCard.ordinal).toLong())
        if (!moves.containsKey(hash)) {
            moves[hash] = move
        }
        exploreRoot.game.currentStateIndex = 0
    }
    return moves.values.toList()
}

private inline fun aiClaimRoute(route: Route, root: RootService, emit: (AIService.Move, State) -> Unit){
    when (route) {
        is Ferry -> {
            val currentPlayer = root.game.currentState.currentPlayer

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
            root.playerActionService.claimRoute(route, cards)
            emit(AIService.Move.ClaimRoute(route, cards, null), root.game.currentState)
        }

        is Tunnel -> {
            val used = root.game.currentState.currentPlayer.wagonCards
                .filter { it.color == route.color || it.color == Color.JOKER }
                .shuffled().take(route.length)
            root.playerActionService.claimRoute(route, used)
            if (root.game.gameState != GameState.AFTER_CLAIM_TUNNEL)
                emit(AIService.Move.ClaimRoute(route, used, null), root.game.currentState)
            val required = root.game.currentState.wagonCardsStack.run { subList(max(0, size - 3), size) }
            val used2 = if (used.all { it.color == Color.JOKER }) {
                val requiredCount = required.count { it.color == Color.JOKER }
                val usable = root.game.currentState.currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                if (usable.size < requiredCount) null else usable.take(requiredCount)
            } else {
                val requiredCount = required.count { it.color == Color.JOKER || it.color == route.color }
                val usable =
                    root.game.currentState.currentPlayer.wagonCards.filter { it.color == Color.JOKER || it.color == route.color }
                if (usable.size < requiredCount) null else usable.shuffled().take(requiredCount)
            }
            root.playerActionService.afterClaimTunnel(route, used2)
            emit(AIService.Move.ClaimRoute(route, used, used2), root.game.currentState)
        }

        else -> {
            if (route.color != Color.JOKER) {
                val cards = root.game.currentState.currentPlayer.wagonCards
                    .filter { it.color == route.color }
                    .take(route.length)
                root.playerActionService.claimRoute(route, cards)
                emit(AIService.Move.ClaimRoute(route, cards, null), root.game.currentState)
            } else {
                val counts = root.game.currentState.currentPlayer.wagonCards
                    .groupBy { it.color }.mapValues { it.value.count() }
                val maxFittingCount = counts.filterValues { it >= route.length }
                    .toList().randomOrNull()
                if (maxFittingCount != null) {
                    val cards = root.game.currentState.currentPlayer.wagonCards
                        .filter { it.color == maxFittingCount.first }
                        .take(route.length)
                    root.playerActionService.claimRoute(route, cards)
                    emit(AIService.Move.ClaimRoute(route, cards, null), root.game.currentState)
                } else if (route.isMurmanskLieksa()) {
                    val maxCount = counts.maxByOrNull { it.value }
                    checkNotNull(maxCount)
                    val required = 9 - maxCount.value
                    val state = root.game.currentState
                    val used = state.currentPlayer.wagonCards.filter { it.color == maxCount.key }.toMutableList()
                    val left = state.currentPlayer.wagonCards.filter { it.color != maxCount.key }.shuffled()
                    used.addAll(left.subList(0, required * 4))
                    root.playerActionService.claimRoute(route, used)
                    emit(AIService.Move.ClaimRoute(route, used, null), root.game.currentState)
                } else {
                    throw AssertionError("Invalid claim")
                }
            }
        }
    }
}

private inline fun minMaxDrawDestinationCard(root: RootService, emit: (AIService.Move, State) -> Unit) {
    val valid = (0 until min(root.game.currentState.destinationCards.size, 3)).shuffled()
    val removeIndices = if (valid.size == 1) 0 else nextInt(0, valid.size - 1)
    val usedIndices = valid.subList(0, valid.size - removeIndices)
    root.playerActionService.drawDestinationCards(usedIndices)
    emit(AIService.Move.DrawDestinationCard(usedIndices), root.game.currentState)
}

