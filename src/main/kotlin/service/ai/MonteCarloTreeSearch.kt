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
    var precomputedChoices: PrecomputedChoices?,
    var winner: Player?,
    var children: List<GameTree>? = null,
    var wonCount: AtomicInteger = AtomicInteger(0),
    var visited: AtomicInteger = AtomicInteger(0),
    var score: Double = 100000.0
)

private data class PrecomputedChoices(
    val destinationCards: List<AIMove.DrawDestinationCard>,
    val drawableWagonCards: List<AIMove.DrawWagonCard>,
    val unclaimedRoutes: List<Route>,
) {
    fun choiceCount() =
        destinationCards.size + drawableWagonCards.size
}

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
    val move = game.currentState.findMoveMonteCarlo(c, timeLimit)
    executeMontyMove(move)
}

private fun State.findMoveMonteCarlo(c: Double, timeLimit: Int): AIMove {
    // pre-compute all claimed routes since this speeds up the computation for the routes, claimable by each player,
    // considerably
    val startingChoices = run {
        val routes = IdentityHashMap<Route, Unit>(79)
        cities.flatMap { it.routes }.forEach { routes[it] = Unit }
        val doubleClaimAllowed = players.size > 2
        players.flatMap { it.claimedRoutes }.forEach {
            routes.remove(it)
            if (it.sibling != null && !doubleClaimAllowed) {
                routes.remove(it.sibling)
            }
        }
        val unclaimedRoutes = routes.keys.toList()
        val destinationIndices = destinationIndices()
        val drawableWagonCards = uniqueDrawWagonCard(this)
        PrecomputedChoices(destinationIndices, drawableWagonCards, unclaimedRoutes)
    }
    val mainPlayer = currentPlayer
    // the playable moves, aka. the moves whose effectiveness should be evaluated
    val options = monteCarloChildren(null, startingChoices)
    options.forEach {
        val state = it.state(this)
        it.precomputedChoices(state, startingChoices)
    }
    // if one of the starting moves leads to victory, use it without further evalutation
    val winningMove = options.firstOrNull { it.winner?.name === mainPlayer.name }
    if (winningMove != null) {
        return winningMove.move
    }
    //play as many games as the time allows
    val start = System.currentTimeMillis()
    var operations = 0
    while (System.currentTimeMillis() - start < timeLimit) {
        val selected = checkNotNull(options.selectNext())
        playoff(c, ++operations, selected, mainPlayer)
        operations++
    }
    println(operations)
    return checkNotNull(options.maxByOrNull { it.wonCount.get() }).move
}

/**
 * One game instance, played to the end
 * @param operations the number of overall operations
 */
private fun playoff(c: Double, operations: Int, rootNode: GameTree, mainPlayer: Player) {
    var current = rootNode
    while (current.winner == null) {
        current.visited.incrementAndGet()
        // Select the next move the visit
        val child = current.children().selectNext()
        checkNotNull(child)
        // if the move has not been visited, calculate the resulting state
        if (child.state == null) child.state(checkNotNull(current.state))
        // recalculate the score
        current.renewScore(c, current.parent?.visited?.get() ?: operations)
        current = child
    }
    if (current.winner?.name == mainPlayer.name) {
        while (true) {
            val parent = current.parent
            current.wonCount.incrementAndGet()
            val parentVisited = parent?.visited?.get() ?: operations
            current.renewScore(c, parentVisited)
            if (parent != null) current = parent else break
        }
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
private fun GameTree.state(parentState: State): State {
    val exploreRoot = RootService()
    val reporter = WinnerReporter()
    exploreRoot.game = Game(parentState)
    exploreRoot.gameService.addRefreshable(reporter)
    exploreRoot.executeMontyMove(move)
    winner = reporter.report()
    return exploreRoot.game.currentState.also { this.state = it }
}

private fun GameTree.precomputedChoices(state: State, parentChoices: PrecomputedChoices): PrecomputedChoices {
    fun <T> List<T>.without(instance: T): List<T> {
        var result = this
        for (i in indices) {
            if (get(i) === instance) {
                result = ArrayList<T>(size - 1).also {
                    it.addAll(subList(0, i))
                    if (i + 1 < size) it.addAll(subList(i + 1, size))
                }
                break
            }
        }
        return result
    }
    precomputedChoices?.also { return@precomputedChoices it }
    val currentPlayerIndex = if (state.currentPlayerIndex < 1) state.players.size - 1 else state.currentPlayerIndex - 1
    val currentPlayer = state.players[currentPlayerIndex]
    val choices = when (move) {
        is AIMove.ClaimRoute -> {
            var newUnclaimed = parentChoices.unclaimedRoutes
            var newDraw = parentChoices.drawableWagonCards
            if (move.route !is Tunnel || (currentPlayer.claimedRoutes.any { it === move.route })) {
                val allowDoubleRoutes = state.players.size > 2
                newUnclaimed = if (move.route.sibling == null || allowDoubleRoutes) {
                    newUnclaimed.without(move.route)
                } else {
                    val oldUnclaimed = newUnclaimed
                    newUnclaimed = ArrayList(newUnclaimed.size - 1)
                    for (it in oldUnclaimed) {
                        if (it !== move.route && it !== move.route.sibling) newUnclaimed.add(it)
                    }
                    newUnclaimed
                }
            }
            if (move.route is Tunnel) {
                newDraw = uniqueDrawWagonCard(state)
            }
            parentChoices.copy(
                drawableWagonCards = newDraw,
                unclaimedRoutes = newUnclaimed,
            )
        }

        is AIMove.DrawDestinationCard -> {
            if (state.destinationCards.size < 3) {
                parentChoices.copy(destinationCards = state.destinationIndices())
            } else {
                parentChoices
            }
        }

        is AIMove.DrawWagonCard -> {
            val newDrawableCards = uniqueDrawWagonCard(state)
            parentChoices.copy(drawableWagonCards = newDrawableCards)
        }
    }
    precomputedChoices = choices
    require(choices.unclaimedRoutes.isNotEmpty())
    return choices
}

/**
 * returns [GameTree.children] if not null, otherwise calculates children using [monteCarloChildren]. Calculates the
 * state if necessary
 */
private fun GameTree.children(): List<GameTree> {
    val children1 = children
    if (children1 != null) return children1
    val state = state ?: state(checkNotNull(parent?.state))
    val precomputedChoices = precomputedChoices ?: precomputedChoices(state, checkNotNull(parent?.precomputedChoices))
    val children2 = state.monteCarloChildren(this, precomputedChoices)
    children = children2
    return children2
}

// computes the possible actions to take from the current state and adds them to list
private fun State.monteCarloChildren(
    parent: GameTree?,
    choices: PrecomputedChoices,
): List<GameTree> {
    val children = ArrayList<GameTree>(parent?.children?.size ?: choices.choiceCount())
    choices.destinationCards.forEach {
        children += GameTree(parent, it, null, null, null, wonCount = AtomicInteger(0))
    }
    choices.drawableWagonCards.forEach {
        children += GameTree(parent, it, null, null, null, wonCount = AtomicInteger(0))
    }
    val exploreRoot = RootService().also { it.game = Game(this) }
    val currentPlayer = currentPlayer
    val doubleRoutes = players.size > 2
    exploreRoot.claimRoutesMoves(choices.unclaimedRoutes, currentPlayer, doubleRoutes) {
        children += GameTree(parent, it, null, null, null, wonCount = AtomicInteger(0))
    }
    children.shuffle()
    return children
}

private inline fun RootService.claimRoutesMoves(
    unclaimedRoutes: List<Route>,
    currentPlayer: Player,
    doubleRoutes: Boolean,
    emit: (AIMove.ClaimRoute) -> Unit
) {
    val cardCount = currentPlayer.wagonCards.size
    val counts = currentPlayer.wagonCards.counts()
    var maxWithoutLocomotive = Color.BLUE
    var maxWithLocomotive = Color.BLUE

    for (color in Color.values()) {
        if (counts[color.ordinal] > counts[maxWithLocomotive.ordinal])
            maxWithLocomotive = color
        if (color != Color.JOKER && counts[color.ordinal] > counts[maxWithoutLocomotive.ordinal])
            maxWithoutLocomotive = color
    }
    val coloredCards = Color.values().map { color ->
        if (counts[color.ordinal] > 0)
            currentPlayer.wagonCards.filterTo(ArrayList(counts[color.ordinal])) { it.color == color }
        else
            emptyList()
    }
    val locomotiveCount = if (maxWithLocomotive == Color.JOKER) 0 else counts[Color.JOKER.ordinal]
    for (route in unclaimedRoutes) {
        if (currentPlayer.trainCarsAmount < route.completeLength) continue
        val canClaim = when (route) {
            is Ferry -> {
                val color = if (route.color != Color.JOKER) route.color else maxWithoutLocomotive
                val colorCards = counts[color.ordinal]
                val realLocomotiveCount = counts[Color.JOKER.ordinal]
                val otherCardCount = cardCount - colorCards - realLocomotiveCount
                playerActionService.canClaimWithCounts(route, colorCards, realLocomotiveCount, otherCardCount, false)
            }

            else -> {
                val color = if (route.color != Color.JOKER) route.color else maxWithLocomotive
                val colorCards = counts[color.ordinal]
                val otherCardCount = cardCount - colorCards - locomotiveCount
                playerActionService.canClaimWithCounts(route, colorCards, locomotiveCount, otherCardCount, false)
            }
        }
        if (!canClaim || (doubleRoutes && currentPlayer.claimedRoutes.any { route === it })) continue
        monteCarloClaimRoute(route, coloredCards) {
            emit(it)
            game.gameState = GameState.DEFAULT
            game.currentStateIndex = 0
        }
    }
}
/**
* Claims a route with behavior specific to monte-carlo search's strength
*/
private inline fun RootService.monteCarloClaimRoute(
    route: Route,
    colorLists: List<List<WagonCard>>,
    emit: (AIMove.ClaimRoute) -> Unit
) {
    when (route) {
        is Ferry -> {
            val currentPlayer = game.currentState.currentPlayer
            // if the ferry is gray, try all possible colors as main colors of the cards used to pay the route
            if (route.color == Color.JOKER) {
                val allLocomotiveCards = colorLists[Color.JOKER.ordinal]
                for (primaryColor in Color.values()) {
                    if (primaryColor == Color.JOKER) continue
                    val allPrimaryCards = colorLists[primaryColor.ordinal]
                    val primaryCards = allPrimaryCards.subList(0, min(route.length, allPrimaryCards.size))
                    val locomotiveCards = allLocomotiveCards
                        .subList(0, min(route.ferries + (route.length - primaryCards.size), allLocomotiveCards.size))
                    var cards = primaryCards + locomotiveCards
                    if (cards.size < route.completeLength) {
                        val remainingPrimaries = allPrimaryCards.subList(primaryCards.size, allPrimaryCards.size)
                        val rest = currentPlayer.wagonCards.filterOther(
                            remainingPrimaries, primaryColor, allPrimaryCards.size, allLocomotiveCards.size
                        )
                        cards = cards + rest.shuffled().take((route.completeLength - cards.size) * 3)
                    }
                    // if the route cannot be claimed with the cards given, do not emit the move
                    if (playerActionService.canClaimRoute(route, cards, true)) {
                        emit(AIMove.ClaimRoute(route, cards, null))
                    }
                }
            } else {
                val primaryColor = route.color
                val allPrimaryCards = colorLists[primaryColor.ordinal]
                val primaryCards = allPrimaryCards.subList(0, min(route.length, allPrimaryCards.size))
                val allLocomotiveCards = colorLists[Color.JOKER.ordinal]
                val locomotiveCards = allLocomotiveCards
                    .subList(0, min(allLocomotiveCards.size, route.ferries + (route.length - primaryCards.size)))
                var cards = primaryCards + locomotiveCards
                if (cards.size < route.completeLength) {
                    val remainingPrimaries = allPrimaryCards.subList(primaryCards.size, allPrimaryCards.size)
                    val rest = currentPlayer.wagonCards
                        .filterOther(remainingPrimaries, primaryColor, allPrimaryCards.size, allLocomotiveCards.size)
                    cards = cards + rest.shuffled().subList(0, (route.completeLength - cards.size) * 3)
                }
                emit(AIMove.ClaimRoute(route, cards, null))
            }
        }

        is Tunnel -> {
            //prioritise using colored cards over using locomotives
            val allPrimaryCards = colorLists[route.color.ordinal]
            var used = allPrimaryCards.subList(0, min(route.length, allPrimaryCards.size))
            if (used.size < route.length) {
                val allLocomotive = colorLists[Color.JOKER.ordinal]
                used = used + allLocomotive.subList(0, min(route.length - used.size, allLocomotive.size))
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
                val allPrimaryCards = colorLists[route.color.ordinal]
                val cards = allPrimaryCards.subList(0, route.length)
                emit(AIMove.ClaimRoute(route, cards, null))
            } else {
                //if the route is gray, try all colors to see if they can be used, if they can be used, emit them
                var hasFitting = false
                for (color in Color.values()) {
                    val primaryCards = colorLists[color.ordinal]
                    if (primaryCards.size < route.length) continue
                    emit(AIMove.ClaimRoute(route, primaryCards.subList(0, route.length), null))
                    hasFitting = true
                }
                if (!hasFitting) {
                    if (route.isMurmanskLieksa()) {
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
}

/**
 * Claims a route with behavior specific to monte-carlo search's strength
 */
private inline fun RootService.monteCarloClaimRoute(route: Route, emit: (AIMove.ClaimRoute) -> Unit) {
    when (route) {
        is Ferry -> {
            val currentPlayer = game.currentState.currentPlayer
            // if the ferry is gray, try all possible colors as main colors of the cards used to pay the route
            if (route.color == Color.JOKER) {
                val counts = currentPlayer.wagonCards.counts()
                val allLocomotiveCards = currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                for (primaryColor in Color.values()) {
                    if (primaryColor == Color.JOKER) continue
                    val allPrimaryCards = currentPlayer.wagonCards
                        .filterTo(ArrayList(counts[primaryColor.ordinal])) { it.color == primaryColor }
                    val primaryCards = allPrimaryCards
                        .subList(0, min(route.length, allPrimaryCards.size))
                    val locomotiveCards = allLocomotiveCards
                        .subList(0, min(route.ferries + (route.length - primaryCards.size), allLocomotiveCards.size))
                    var cards = primaryCards + locomotiveCards
                    if (cards.size < route.completeLength) {
                        val remainingPrimaries = allPrimaryCards.subList(primaryCards.size, allPrimaryCards.size)
                        val rest = currentPlayer.wagonCards.filterOther(
                            remainingPrimaries, primaryColor, allPrimaryCards.size, allLocomotiveCards.size
                        )
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
                val allLocomotiveCards = currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                val locomotiveCards = allLocomotiveCards
                    .subList(0, min(allLocomotiveCards.size, route.ferries + (route.length - primaryCards.size)))
                var cards = primaryCards + locomotiveCards
                if (cards.size < route.completeLength) {
                    val remainingPrimaries = allPrimaryCards.subList(primaryCards.size, allPrimaryCards.size)
                    val rest = currentPlayer.wagonCards
                        .filterOther(remainingPrimaries, primaryColor, allPrimaryCards.size, allLocomotiveCards.size)
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
                val counts = game.currentState.currentPlayer.wagonCards.counts()
                var hasFitting = false
                for (color in Color.values()) {
                    if (counts[color.ordinal] < route.length) continue
                    val cards = game.currentState.currentPlayer.wagonCards
                        .filterTo(ArrayList(counts[color.ordinal])) { it.color == color }
                        .take(route.length)
                    emit(AIMove.ClaimRoute(route, cards, null))
                    hasFitting = true
                }
                if (!hasFitting) {
                    if (route.isMurmanskLieksa()) {
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

private fun List<WagonCard>.filterOther(
    remainingPrimaries: List<WagonCard>,
    primaryColor: Color,
    primaryColorCount: Int,
    locomotiveCount: Int
): List<WagonCard> {
    val target = ArrayList<WagonCard>(size - primaryColorCount - locomotiveCount + remainingPrimaries.size)
    filterTo(target) {
        it.color != Color.JOKER && it.color != primaryColor
    }
    target.addAll(remainingPrimaries)
    return target
}