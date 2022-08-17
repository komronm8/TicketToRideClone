package service

import entity.*
import view.Refreshable
import java.util.*
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

    fun randomNextTurn() {
        val drawWagonCards = if (state.run { wagonCardsStack.size + discardStack.size } >= 2) {
            (0..6).flatMap { m1 -> (0..6).map { m2 -> AIMove.DrawWagonCard(m1, m2) } }
        } else {
            emptyList()
        }
        val routes = IdentityHashMap<Route, Unit>(79)
        state.cities.flatMap { it.routes }.forEach { routes[it] = Unit }
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
        val actionSpace = drawWagonCards + validRoutes + destinationDrawAction
        val action = actionSpace.random()
        println(action)
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
//        val action = nextInt(0 ,3)
//        when (action) {
//            0 -> randomDrawWagonCard()
//            1 -> randomDrawDestinationCard()
//            2 -> {}
//        }
    }
    /*fun randomDrawWagonCard(){
        root.playerActionService.drawWagonCard(nextInt(0,6))
        root.playerActionService.drawWagonCard(nextInt(0,6))
    }*/

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
                println(cards)
                println(route)
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
                        println(used)
                        root.playerActionService.claimRoute(route, used)
                    } else {
                        throw AssertionError("Invalid claim")
                    }
                }
            }
        }
    }

    fun randomAIGame() {
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
            override fun refreshAfterEndGame() {
                ended = true
            }
        }
        root.addRefreshable(refreshable)
        val aiService = AIService(root)
        while (!refreshable.ended) {
            aiService.randomNextTurn()
        }
    }
}

