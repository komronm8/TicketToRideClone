package service.ai

import entity.*
import service.RootService
import view.Refreshable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private data class GameTree(
    val parent: GameTree?,
    val move: AIMove,
    var state: State?,
    var winner: Player?,
    var children: List<GameTree>? = null,
    var wonCount: AtomicInteger = AtomicInteger(0),
    var visited: AtomicInteger = AtomicInteger(0),
    var score: Double = 100000.0
)

private class WinnerReporter(private var winner: Player? = null) : Refreshable {
    override fun refreshAfterEndGame(winner: Player) {
        this.winner = winner
    }

    fun report(): Player? = winner.also { winner = null }
}

/**
 * Finds the best move according to the monte-carlo search tree algorithm
 * @param c the exploration factor: the algorithm favors moves which have high win-rate,
 * but the exploration factor allows it, to visit rarely visited moves
 * @param timeLimit the time limit for the calculation
 */
fun RootService.monteCarloMove(c: Double, timeLimit: Int) {
    // pre-compute all claimed routes since this speeds up the computation for the routes, claimable by each player,
    // considerably
    val unclaimedRoutes = run {
        val routes = IdentityHashMap<Route, Unit>(79)
        game.currentState.cities.flatMap { it.routes }.forEach { routes[it] = Unit }
        val doubleClaimAllowed = game.currentState.players.size > 2
        game.currentState.players.flatMap { it.claimedRoutes }.forEach {
            routes.remove(it)
            if (it.sibling != null && !doubleClaimAllowed) {
                routes.remove(it.sibling)
            }
        }
        routes.keys.toList()
    }
    val mainPlayer = game.currentState.currentPlayer
    // the playable moves, aka. the moves whose effectiveness should be evaluated
    val options = this.game.currentState.monteCarloChildren(null, unclaimedRoutes)
    options.forEach { it.state(this.game.currentState, mainPlayer) }
    // if one of the starting moves leads to victory, use it without further evalutation
    val winningMove = options.firstOrNull { it.winner?.name === mainPlayer.name }
    if (winningMove != null) {
        executeMontyMove(winningMove.move)
        return
    }
    //play as many games as the time allows
    val start = System.currentTimeMillis()
    var operations = 0
    while (System.currentTimeMillis() - start < timeLimit) {
        val selected = checkNotNull(options.selectNext())
        playoff(c, ++operations, selected, unclaimedRoutes, mainPlayer)
        operations++
    }
    println(operations)
    executeMontyMove(checkNotNull(options.maxByOrNull { it.wonCount.get() }).move)
}

/**
 * One game instance, played to the end
 * @param operations the number of overall operations
 */
private fun playoff(c: Double, operations: Int, rootNode: GameTree, claimableRoutes: List<Route>, mainPlayer: Player) {
    var current = rootNode
    while (current.winner == null) {
        current.visited.incrementAndGet()
        // Select the next move the visit
        val child = current.children(claimableRoutes, mainPlayer).selectNext()
        checkNotNull(child)
        // if the move has not been visited, calculate the resulting state
        if (child.state == null) child.state(checkNotNull(current.state), mainPlayer)
        // recalculate the score
        current.renewScore(c, current.parent?.visited?.get() ?: operations)
        current = child
    }
}


private fun GameTree.renewScore(c: Double, parentVisited: Int) {
    val exploitation = (wonCount.get().toDouble() / (visited.get() + 1).toDouble())
    val exploration = c * sqrt(ln(parentVisited.toDouble()) / visited.get().plus(1).toDouble())
    score = exploitation + exploration
}

private fun List<GameTree>.selectNext() = maxByOrNull { it.score }

/**
computes the state resulting from the [GameTree.move], increases wonCount if necessary
 */
private fun GameTree.state(parentState: State, mainPlayer: Player): State {
    val root = RootService()
    val reporter = WinnerReporter()
    root.game = Game(parentState)
    root.gameService.addRefreshable(reporter)
    root.executeMontyMove(move)
    winner = reporter.report()
    if (winner?.name == mainPlayer.name) {
        var head: GameTree? = this
        while (head != null) {
            head.wonCount.incrementAndGet()
            head = head.parent
        }
    }
    return root.game.currentState.also { this.state = it }
}

/**
 * returns [GameTree.children] if not null, otherwise calculates children using [monteCarloChildren]. Calculates the
 * state if necessary
 */
private fun GameTree.children(claimableRoutes: List<Route>, mainPlayer: Player): List<GameTree> {
    val children1 = children
    if (children1 != null) return children1
    val state = state ?: state(checkNotNull(parent?.state), mainPlayer)
    val children2 = state.monteCarloChildren(this, claimableRoutes)
    children = children2
    return children2
}

/**
 * Calculates all routes, that can be claimed by the current player in [exploreRoot] and executes [with] with them
 * @param claimableRoutes all routes that are not claimed and could be claimed with the right cards
 */
private inline fun GameTree?.allClaimableRoutes(
    exploreRoot: RootService,
    claimableRoutes: List<Route>,
    with: (Route) -> Unit
) {
    val currentPlayer = exploreRoot.game.currentState.currentPlayer
    val allowClaimDouble = exploreRoot.game.currentState.players.size > 2
    for (route in claimableRoutes) {
        var canClaim = true
        var head: GameTree? = this
        // claimable routes is not updated as we go further down the game tree which is why we go up the tree
        // to check if the route has been claimed in the meantime
        while (head != null) {
            val move = head.move
            if (move is AIMove.ClaimRoute &&
                (move.route === route || (!allowClaimDouble && move.route.sibling === route))
            ) {
                canClaim = false
                break
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

// computes the possible actions to take from the current state and adds them to list
private fun State.monteCarloChildren(
    parent: GameTree?,
    claimableRoutes: List<Route>,
): List<GameTree> {
    val children = mutableListOf<GameTree>()
    RootService().also { it.game = Game(this) }.monteCarloMove(parent, claimableRoutes) { move ->
        children += GameTree(parent, move, null, null, wonCount = AtomicInteger(0))
    }
    return children
}

/**
 * computes the possible moves and calls [emit] with each of them
 */
private inline fun RootService.monteCarloMove(
    parent: GameTree?,
    claimableRoutes: List<Route>,
    emit: (AIMove) -> Unit
) {
    val exploreRoot = RootService().also { it.game = Game(this.game.currentState) }
    parent.allClaimableRoutes(this, claimableRoutes) {
        exploreRoot.monteCarloClaimRoute(it) { move ->
            emit(move)
            exploreRoot.game.gameState = GameState.DEFAULT
            exploreRoot.game.currentStateIndex = 0
        }
    }
    uniqueDrawWagonCard(this).forEach {
        emit(AIMove.DrawWagonCard(it.firstDraw, it.secondDraw))
    }
    this.game.currentState.destinationIndices {
        emit(AIMove.DrawDestinationCard(it))
    }
}


private val wagonCardMoves = ArrayList<AIMove.DrawWagonCard>(3).apply {
    add(AIMove.DrawWagonCard(5, 5))
    for (i in 0..4) {
        add(AIMove.DrawWagonCard(i, 5))
        add(AIMove.DrawWagonCard(5, i))
        for (j in i..4) {
            add(AIMove.DrawWagonCard(i, j))
        }
    }
}

/**
 * computes draw wagon card moves with unique effects
 */
private fun uniqueDrawWagonCard(exploreRoot: RootService): List<AIMove.DrawWagonCard> {
    val canDraw = exploreRoot.game.currentState.run { wagonCardsStack.size + discardStack.size } >= 2
    val drawWagonCards = if (canDraw) {
        wagonCardMoves
    } else {
        return emptyList()
    }
    val moves: HashMap<Long, AIMove.DrawWagonCard> = HashMap(20)
    val countArray = IntArray(9) { 0 }
    val drawStack = exploreRoot.game.currentState.wagonCardsStack
    val openCards = exploreRoot.game.currentState.openCards

    if (drawStack.size < 2) {
        return drawWagonCards
    }

    for (move in drawWagonCards) {
        val firstDrawCard = drawStack[drawStack.size - 1]
        val secondDrawCard = drawStack[drawStack.size - 2]

        for (card in openCards) {
            countArray[card.color.ordinal] += 1
        }

        var firstCard: Color
        var secondCard: Color

        if (move.firstDraw in 0..4) {
            firstCard = openCards[move.firstDraw].color
            countArray[firstCard.ordinal] -= 1
            countArray[firstDrawCard.color.ordinal] += 1
        } else {
            firstCard = firstDrawCard.color
        }
        if (move.secondDraw in 0..4) {
            secondCard = if (move.secondDraw == move.firstDraw) {
                firstDrawCard.color
            } else {
                openCards[move.secondDraw].color
            }
            countArray[secondCard.ordinal] -= 1
            countArray[secondDrawCard.color.ordinal] += 1
        } else {
            secondCard = secondDrawCard.color
        }

        var hash: Long = 0
        var factor: Long = 1
        for (i in 0 until 9) {
            hash += countArray[i] * factor
            factor *= 9
            countArray[i] = 0
        }
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

/**
 * Claims a route with behavior specific to monte-carlo search's strength
 */
private inline fun RootService.monteCarloClaimRoute(route: Route, emit: (AIMove) -> Unit) {
    when (route) {
        is Ferry -> {
            val currentPlayer = game.currentState.currentPlayer
            val counts = currentPlayer.wagonCards.groupBy { it.color }.mapValues { it.value.count() }.toMutableMap()
            // if the ferry is gray, try all possible colors as main colors of the cards used to pay the route
            if (route.color == Color.JOKER) {
                for (primaryColor in counts.keys) {
                    if (primaryColor == Color.JOKER) continue
                    val allPrimaryCards = currentPlayer.wagonCards.filter { it.color == primaryColor }
                    val primaryCards = allPrimaryCards.subList(0, min(route.length, allPrimaryCards.size))
                    val locomotiveCards = currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                        .take(route.ferries + (route.length - primaryCards.size))
                    var cards = primaryCards + locomotiveCards
                    if (cards.size < route.completeLength) {
                        val rest =
                            currentPlayer.wagonCards.filter { it.color != Color.JOKER && it.color != primaryColor } +
                                    allPrimaryCards.subList(primaryCards.size, allPrimaryCards.size)
                        cards = cards + rest.shuffled().take((route.completeLength - cards.size) * 3)
                    }
                    // if the route cannot be claimed with the cards given, do not emit the move
                    if (playerActionService.canClaimRoute(route, cards, true)) {
                        emit(AIMove.ClaimRoute(route, cards, null))
                    }
                }
            } else {
                val primaryColor = route.color
                val allPrimaryCards = currentPlayer.wagonCards.filter { it.color == primaryColor }
                val primaryCards = allPrimaryCards.subList(0, min(route.length, allPrimaryCards.size))
                val locomotiveCards = currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                    .take(route.ferries + (route.length - primaryCards.size))
                var cards = primaryCards + locomotiveCards
                if (cards.size < route.completeLength) {
                    val rest = currentPlayer.wagonCards.filter { it.color != Color.JOKER && it.color != primaryColor } +
                            allPrimaryCards.subList(primaryCards.size, allPrimaryCards.size)
                    cards = cards + rest.shuffled().subList(0, (route.completeLength - cards.size) * 3)
                }
                emit(AIMove.ClaimRoute(route, cards, null))
            }
        }

        is Tunnel -> {
            //prioritise using colored cards over using locomotives
            var used = game.currentState.currentPlayer.wagonCards
                .filter { it.color == route.color }
                .take(route.length)
            if (used.size < route.length) {
                used = used + game.currentState.currentPlayer.wagonCards
                    .filter { it.color == Color.JOKER }
                    .take(route.length - used.size)
            }
            playerActionService.claimRoute(route, used)
            if (game.gameState != GameState.AFTER_CLAIM_TUNNEL) {
                emit(AIMove.ClaimRoute(route, used, null))
                return
            }
            val used2 = game.currentState.monteCarloPayTunnel(route, used)
            emit(AIMove.ClaimRoute(route, used, used2))
        }

        else -> {
            //if the route is not gray, then simply use the viable cards
            if (route.color != Color.JOKER) {
                val cards = game.currentState.currentPlayer.wagonCards
                    .filter { it.color == route.color }
                    .take(route.length)
                emit(AIMove.ClaimRoute(route, cards, null))
            } else {
                //if the route is gray, try all colors to see if they can be used, if they can be used, emit them
                val counts = game.currentState.currentPlayer.wagonCards
                    .groupBy { it.color }
                    .mapValues { it.value.count() }
                val maxFittingCount = counts.filterValues { it >= route.length }
                if (maxFittingCount.isNotEmpty()) {
                    for (color in maxFittingCount.keys) {
                        val cards = game.currentState.currentPlayer.wagonCards
                            .filter { it.color == color }
                            .take(route.length)
                        emit(AIMove.ClaimRoute(route, cards, null))
                    }
                } else if (route.isMurmanskLieksa()) {
                    /*val cardCount = game.currentState.currentPlayer.wagonCards.size
                    val viableValues = counts.filterValues { (cardCount - it) / 4 + it >= 9 }
                    for ((color, count) in viableValues.entries) {
                        val required = 9 - count
                        val state = game.currentState
                        val used = state.currentPlayer.wagonCards.filter { it.color == color }.toMutableList()
                        val left = state.currentPlayer.wagonCards.filter { it.color != color }.shuffled()
                        used.addAll(left.subList(0, required * 4))
                        playerActionService.claimRoute(route, used)
                        emit(AIMove.ClaimRoute(route, used, null))
                    }*/
                } else {
                    throw AssertionError("Invalid claim")
                }
            }
        }
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

/**
computes all valid destination indices for the current state in regard to the amount of cards left
in [State.destinationCards]
 */
private inline fun State.destinationIndices(emit: (List<Int>) -> Unit) {
    val drawAmount = min(destinationCards.size, 3)
    if (drawAmount == 0) return
    val indices = (0 until drawAmount).toList()
    if (drawAmount == 1) emit(indices)
    for (i in 0 until drawAmount) {
        emit(listOf(indices[i]))
        if (i + 1 < drawAmount) emit(indices.subList(i, i + 2))
    }
    if (drawAmount == 3) emit(listOf(0, 2))
}

/**
 * execute the move on the given root service
 */
private fun RootService.executeMontyMove(move: AIMove) {
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

