package service.ai

import entity.*
import service.RootService
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Makes a random turn
 */
fun RootService.randomNextTurn() {
    val actionSpace = game.currentState.getPossibleMoves()
    when (val action = actionSpace.random()) {
        is RandomMove.ClaimRoute -> {
            randomClaimRoute(action.route)
        }

        RandomMove.DrawDestinationCard -> {
            randomDrawDestinationCard()
        }

        is RandomMove.DrawWagonCard -> {
            playerActionService.run {
                drawWagonCard(action.firstDraw)
                drawWagonCard(action.secondDraw)
            }
        }
    }
}

/**
 * An AI move intention
 */
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

private fun RootService.randomDrawDestinationCard() {
    val valid = (0 until min(game.currentState.destinationCards.size, 3)).toMutableList().apply { shuffle() }
    val removeIndices = if (valid.size == 1) 0 else Random.nextInt(0, valid.size - 1)
    playerActionService.drawDestinationCards(valid.subList(0, valid.size - removeIndices))
}

private fun RootService.randomClaimRoute(route: Route) {
    when (route) {
        is Ferry -> {
            val state = game.currentState
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
            playerActionService.claimRoute(route, cards)
        }

        is Tunnel -> {
            val used = game.currentState.currentPlayer.wagonCards
                .filter { it.color == route.color || it.color == Color.JOKER }
                .shuffled().take(route.length)
            playerActionService.claimRoute(route, used)
            if (game.gameState != GameState.AFTER_CLAIM_TUNNEL) return
            if (Random.nextInt(0, 4) == 0) {
                playerActionService.afterClaimTunnel(route, null)
                return
            }
            val state = game.currentState
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
            playerActionService.afterClaimTunnel(route, used2)
        }

        else -> {
            val state = game.currentState
            if (route.color != Color.JOKER) {
                val cards = state.currentPlayer.wagonCards
                    .filter { it.color == route.color }
                    .take(route.length)
                playerActionService.claimRoute(route, cards)
            } else {
                val counts = game.currentState.currentPlayer.wagonCards
                    .groupBy { it.color }.mapValues { it.value.count() }
                val maxFittingCount = counts.filterValues { it >= route.length }
                    .toList().randomOrNull()
                if (maxFittingCount != null) {
                    val cards = game.currentState.currentPlayer.wagonCards
                        .filter { it.color == maxFittingCount.first }
                        .take(route.length)
                    playerActionService.claimRoute(route, cards)
                } else if (route.isMurmanskLieksa()) {
                    val maxCount = counts.maxByOrNull { it.value }
                    checkNotNull(maxCount)
                    val required = 9 - maxCount.value
                    val used = state.currentPlayer.wagonCards.filter { it.color == maxCount.key }.toMutableList()
                    val left = state.currentPlayer.wagonCards.filter { it.color != maxCount.key }.shuffled()
                    used.addAll(left.subList(0, required * 4))
                    playerActionService.claimRoute(route, used)
                } else {
                    throw AssertionError("Invalid claim")
                }
            }
        }
    }
}