package service.ai

import entity.*
import service.RootService
import view.Refreshable
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min

private data class GameTree(
    val parent: GameTree?,
    val move: AIService.Move,
    val state: State,
    val winner: Player?,
    var children: List<GameTree>? = null,
    var wonCount: AtomicInteger = AtomicInteger(0),
)

private class WinnerReporter(private var winner: Player? = null): Refreshable {
    override fun refreshAfterEndGame(winner: Player) {
        this.winner = winner
    }
    fun report(): Player? = winner.also { winner = null }
}

private fun GameTree.children(claimableRoutes: List<Route>, mainPlayer: Player): List<GameTree> {
    val children1 = children
    if (children1 != null) return children1
    val children2 = this.state.monteCarloChildren(this, claimableRoutes, mainPlayer)
    val childrenWon = children2.count { it.wonCount.get() > 0 }
    if (childrenWon > 0) {
        var head: GameTree? = this
        while (head != null) {
            head.wonCount.addAndGet(childrenWon)
            head = head.parent
        }
    }
    return children2
}

private inline fun GameTree?.allClaimableRoutes(exploreRoot: RootService, claimableRoutes: List<Route>, with: (Route)->Unit) {
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

private fun State.monteCarloChildren(parent: GameTree?, claimableRoutes: List<Route>, mainPlayer: Player): List<GameTree> {
    val children = mutableListOf<GameTree>()
    RootService().also { it.game = Game(this) }.monteCarloMove(parent, claimableRoutes) { move, state, winner ->
        val wonCount = if (winner != null && winner.name === mainPlayer.name) 1 else 0
        children += GameTree(parent, move, state, winner, wonCount = AtomicInteger(wonCount))
    }
    return children
}
private inline fun RootService.monteCarloMove(
    parent: GameTree?,
    claimableRoutes: List<Route>,
    emit: (AIService.Move, State, Player?) -> Unit
) {
    val reporter = WinnerReporter()
    val exploreRoot = RootService().also { it.game = Game(this.game.currentState) }
    parent.allClaimableRoutes(this, claimableRoutes) {
        exploreRoot.monteCarloClaimRoute(it) { move, state ->
            emit(move, state, reporter.report())
            exploreRoot.game.currentStateIndex = 0
        }
    }
    exploreRoot.gameService.addRefreshable(reporter)
    uniqueDrawWagonCard(this).forEach {
        exploreRoot.playerActionService.drawWagonCard(it.firstDraw)
        exploreRoot.playerActionService.drawWagonCard(it.secondDraw)
        emit(AIService.Move.DrawWagonCard(it.firstDraw, it.secondDraw), exploreRoot.game.currentState, reporter.report())
        exploreRoot.game.currentStateIndex = 0
    }
    this.game.currentState.destinationIndices {
        exploreRoot.playerActionService.drawDestinationCards(it)
        emit(AIService.Move.DrawDestinationCard(it), exploreRoot.game.currentState, reporter.report())
        exploreRoot.game.currentStateIndex = 0
    }
}

private inline fun RootService.monteCarloClaimRoute(route: Route, emit: (AIService.Move, State) -> Unit) {
    when (route) {
        is Ferry -> {
            val currentPlayer = game.currentState.currentPlayer

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
            playerActionService.claimRoute(route, cards)
            emit(AIService.Move.ClaimRoute(route, cards, null), game.currentState)
        }

        is Tunnel -> {
            val used = game.currentState.currentPlayer.wagonCards
                .filter { it.color == route.color || it.color == Color.JOKER }
                .shuffled().take(route.length)
            playerActionService.claimRoute(route, used)
            if (game.gameState != GameState.AFTER_CLAIM_TUNNEL)
                emit(AIService.Move.ClaimRoute(route, used, null), game.currentState)
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
            playerActionService.afterClaimTunnel(route, used2)
            emit(AIService.Move.ClaimRoute(route, used, used2), game.currentState)
        }

        else -> {
            if (route.color != Color.JOKER) {
                val cards = game.currentState.currentPlayer.wagonCards
                    .filter { it.color == route.color }
                    .take(route.length)
                playerActionService.claimRoute(route, cards)
                emit(AIService.Move.ClaimRoute(route, cards, null), game.currentState)
            } else {
                val counts = game.currentState.currentPlayer.wagonCards
                    .groupBy { it.color }.mapValues { it.value.count() }
                val maxFittingCount = counts.filterValues { it >= route.length }
                if (maxFittingCount.isNotEmpty()) {
                    for (color in maxFittingCount.keys) {
                        val cards = game.currentState.currentPlayer.wagonCards
                            .filter { it.color == color}
                            .take(route.length)
                        playerActionService.claimRoute(route, cards)
                        emit(AIService.Move.ClaimRoute(route, cards, null), game.currentState)
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

private fun playoff(rootNode: GameTree, claimableRoutes: List<Route>, mainPlayer: Player) {
    var current = rootNode
    while (current.winner == null) {
        current = current.children(claimableRoutes, mainPlayer).random()
    }
}

fun RootService.monteCarloMove() {
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
    val options = this.game.currentState.monteCarloChildren(null, unclaimedRoutes, mainPlayer)
    val winningMove = options.firstOrNull { it.winner?.name === mainPlayer.name }
    if (winningMove != null) {
        executeMove(winningMove.move)
        return
    }
    val operationCounter = AtomicInteger(0)
    val timeLimit = 4000
    val start = Instant.now()
    val threads = (1..Runtime.getRuntime().availableProcessors()).map {
        Thread {
            val start = System.currentTimeMillis()
            for (i in 0..500)  {// langsam oder?
                playoff(options.random(), unclaimedRoutes, mainPlayer)
                operationCounter.incrementAndGet()
            }
        }.apply { start() }
    }
    threads.forEach { it.join() }
    println(Duration.between(start, Instant.now()).toMillis())
    println(operationCounter.get())

    println(checkNotNull(options.maxByOrNull { it.wonCount.get() }).move)
    executeMove(checkNotNull(options.maxByOrNull { it.wonCount.get() }).move)
}

