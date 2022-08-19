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
    val move: AIService.Move,
    var state: State?,
    var winner: Player?,
    var children: List<GameTree>? = null,
    var wonCount: AtomicInteger = AtomicInteger(0),
    var visited: AtomicInteger = AtomicInteger(0)
)

private class WinnerReporter(private var winner: Player? = null) : Refreshable {
    override fun refreshAfterEndGame(winner: Player) {
        this.winner = winner
    }

    fun report(): Player? = winner.also { winner = null }
}

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

private fun GameTree.children(claimableRoutes: List<Route>, mainPlayer: Player): List<GameTree> {
    val children1 = children
    if (children1 != null) return children1
    val state = state ?: state(checkNotNull(parent?.state), mainPlayer)
    val children2 = state.monteCarloChildren(this, claimableRoutes)
    return children2
}

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

private inline fun RootService.monteCarloMove(
    parent: GameTree?,
    claimableRoutes: List<Route>,
    emit: (AIService.Move) -> Unit
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
        emit(AIService.Move.DrawWagonCard(it.firstDraw, it.secondDraw))
    }
    this.game.currentState.destinationIndices {
        emit(AIService.Move.DrawDestinationCard(it))
    }
}

private inline fun RootService.monteCarloClaimRoute(route: Route, emit: (AIService.Move) -> Unit) {
    when (route) {
        is Ferry -> {
            val currentPlayer = game.currentState.currentPlayer
            val counts = currentPlayer.wagonCards.groupBy { it.color }.mapValues { it.value.count() }.toMutableMap()
            if (route.color == Color.JOKER) {
                for (primaryColor in counts.keys) {
                    if (primaryColor == Color.JOKER) continue
                    val allPrimaryCards = currentPlayer.wagonCards.filter { it.color == primaryColor }
                    val primaryCards = allPrimaryCards.subList(0, min(route.length, allPrimaryCards.size))
                    val locomotiveCards = currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                        .take(route.ferries + (route.length - primaryCards.size))
                    var cards = primaryCards + locomotiveCards
                    if (cards.size < route.completeLength) {
                        val rest = currentPlayer.wagonCards.filter { it.color != Color.JOKER && it.color != primaryColor } +
                                allPrimaryCards.subList(primaryCards.size, allPrimaryCards.size)
                        cards = cards + rest.shuffled().take((route.completeLength - cards.size) * 3)
                    }
                    if (playerActionService.canClaimRoute(route, cards, true)) {
                        emit(AIService.Move.ClaimRoute(route, cards, null))
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
                emit(AIService.Move.ClaimRoute(route, cards, null))
            }
        }

        is Tunnel -> {

            var used = game.currentState.currentPlayer.wagonCards
                .filter { it.color == route.color}
                .take(route.length)
            if (used.size < route.length) {
               used = used + game.currentState.currentPlayer.wagonCards
                   .filter { it.color == Color.JOKER }
                   .take(route.length - used.size)
            }
            playerActionService.claimRoute(route, used)
            if (game.gameState != GameState.AFTER_CLAIM_TUNNEL) {
                emit(AIService.Move.ClaimRoute(route, used, null))
                return
            }
            val required = game.currentState.wagonCardsStack.run { subList(max(0, size - 3), size) }
            val used2 = if (used.all { it.color == Color.JOKER }) {
                val requiredCount = required.count { it.color == Color.JOKER }
                val usable = game.currentState.currentPlayer.wagonCards.filter { it.color == Color.JOKER }
                if (usable.size < requiredCount) null else usable.take(requiredCount)
            } else {
                val requiredCount = required.count { it.color == Color.JOKER || it.color == route.color }
                val usable =
                    game.currentState.currentPlayer.wagonCards.filter { it.color == Color.JOKER || it.color == route.color }
                if (usable.size < requiredCount) null else usable.shuffled().take(requiredCount)
            }
            emit(AIService.Move.ClaimRoute(route, used, used2))
        }

        else -> {
            if (route.color != Color.JOKER) {
                val cards = game.currentState.currentPlayer.wagonCards
                    .filter { it.color == route.color }
                    .take(route.length)
                emit(AIService.Move.ClaimRoute(route, cards, null))
            } else {
                val counts = game.currentState.currentPlayer.wagonCards
                    .groupBy { it.color }
                    .mapValues { it.value.count() }
                val maxFittingCount = counts.filterValues { it >= route.length }
                if (maxFittingCount.isNotEmpty()) {
                    for (color in maxFittingCount.keys) {
                        val cards = game.currentState.currentPlayer.wagonCards
                            .filter { it.color == color}
                            .take(route.length)
                        emit(AIService.Move.ClaimRoute(route, cards, null))
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
                        emit(AIService.Move.ClaimRoute(route, used, null), game.currentState)
                    }*/
                } else {
                    throw AssertionError("Invalid claim")
                }
            }
        }
    }
}

private fun State.monteCarloPayTunnel(route: Tunnel, used: List<WagonCard>): List<WagonCard>? {
    val required = wagonCardsStack.run { subList(max(0, size - 3), size) }
    if (used.all { it.color == Color.JOKER }) {
        val requiredCount = required.count { it.color == Color.JOKER }
        val usable = currentPlayer.wagonCards.filter { it.color == Color.JOKER }
        return if (usable.size < requiredCount) null else usable.take(requiredCount).also { cards ->
            check(cards.all { it.color == Color.JOKER } && requiredCount == cards.size)
        }
    } else {
        val requiredCount = required.count { it.color == Color.JOKER || it.color == route.color }
        val usable =
            currentPlayer.wagonCards.filter { it.color == Color.JOKER || it.color == route.color }
        return if (usable.size < requiredCount) null else usable.shuffled().take(requiredCount)
    }
}

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

private fun playoff(c: Double, rootNode: GameTree, claimableRoutes: List<Route>, mainPlayer: Player) {
    var current = rootNode
    while (current.winner == null) {
        current.visited.incrementAndGet()
        val parentVisited = ln(current.visited.get().toDouble())
        val child = current.children(claimableRoutes, mainPlayer).selectNext(c, parentVisited)
        checkNotNull(child)
        if (child.state == null) child.state(checkNotNull(current.state), mainPlayer)
        current = child
    }
}

private fun List<GameTree>.selectNext(c: Double, parentVisited: Double) = maxByOrNull {
    val exploitation = (it.wonCount.get().toDouble() / (it.visited.get() + 1).toDouble())
    val exploration = c * sqrt(parentVisited / it.visited.get().plus(1).toDouble())
    exploitation + exploration
}

fun RootService.monteCarloMove(c: Double, timeLimit: Int) {
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
    val options = this.game.currentState.monteCarloChildren(null, unclaimedRoutes)
    options.forEach { it.state(this.game.currentState, mainPlayer) }
    val winningMove = options.firstOrNull { it.winner?.name === mainPlayer.name }
    if (winningMove != null) {
        executeMontyMove(winningMove.move)
        return
    }
    val start = System.currentTimeMillis()
    var operations = 0
    while (System.currentTimeMillis() - start < timeLimit) {
        val selected = checkNotNull(options.selectNext(c, ln(operations.toDouble())))
        playoff(c, selected, unclaimedRoutes, mainPlayer)
        operations++
    }
    println(operations)
    executeMontyMove(checkNotNull(options.maxByOrNull { it.wonCount.get() }).move)
}

private fun RootService.executeMontyMove(move: AIService.Move) {
    when (move) {
        is AIService.Move.ClaimRoute -> {
            playerActionService.claimRoute(move.route, move.usedCards)
            if (game.gameState == GameState.AFTER_CLAIM_TUNNEL) {
                try {
                    playerActionService.afterClaimTunnel(move.route as Tunnel, move.tunnelCards)
                } catch (e: Exception) {
                    playerActionService.afterClaimTunnel(
                        move.route as Tunnel,
                        game.currentState.monteCarloPayTunnel(move.route, move.usedCards)
                    )
                }
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

