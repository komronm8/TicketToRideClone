package service

import entity.*
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame

/**
 * Tests the [PlayerActionService]
 */
class TestPlayerActionService {
    /**
     * Tests the [PlayerActionService.drawWagonCard] method
     */
    @Test
    fun testDrawWagonCard() {
        val discardStack = listOf(WagonCard(Color.YELLOW), WagonCard(Color.BLACK))
        val drawStack = listOf(WagonCard(Color.PURPLE), WagonCard(Color.RED), WagonCard(Color.ORANGE))
        val openCards = listOf(Color.JOKER, Color.BLUE, Color.BLUE, Color.WHITE, Color.GREEN).map(::WagonCard)
        val root = RootService()
        root.game = Game(State(
            emptyList(), openCards, drawStack, discardStack,
            listOf(
                Player(0, "adasd", emptyList(), emptyList(), 40, emptyList(), false),
                Player(0, "wasd", emptyList(), emptyList(), 32, emptyList(), false)
            ),
            cities = constructGraph()
        ))
        root.playerActionService.drawWagonCard(1)
        root.playerActionService.drawWagonCard(-1)
        assertSame(drawStack[2], root.game.currentState.openCards[1], )
        assertSame(drawStack[0], root.game.currentState.wagonCardsStack.lastOrNull())
        assertSame(openCards[1], root.game.currentState.players[0].wagonCards[0])
        assertSame(drawStack[1], root.game.currentState.players[0].wagonCards[1])
        assertEquals(2, root.game.currentState.players[0].wagonCards.size)
        assertEquals(0, root.game.currentState.players[1].wagonCards.size)
        assertEquals(5, root.game.currentState.openCards.size)
        assertEquals(1, root.game.currentState.wagonCardsStack.size)
        root.playerActionService.drawWagonCard(1)
        root.playerActionService.drawWagonCard(-1)
        assertSame(root.game.currentState.openCards[1], drawStack[0])
        assertSame(root.game.currentState.players[1].wagonCards[0], drawStack[2])
        val drawCard = root.game.currentState.players[1].wagonCards[1]
        if (drawCard === discardStack[0]) {
            assertSame(discardStack[1], root.game.currentState.wagonCardsStack.lastOrNull())
        } else if (drawCard === discardStack[1]) {
            assertSame(discardStack[0], root.game.currentState.wagonCardsStack.lastOrNull())
        } else {
            throw AssertionError("Unknown drawn card")
        }
        assertEquals(2, root.game.currentState.players[0].wagonCards.size)
        assertEquals(2, root.game.currentState.players[1].wagonCards.size)
        assertEquals(5, root.game.currentState.openCards.size)
        assertEquals(1, root.game.currentState.wagonCardsStack.size)
        assertEquals(0, root.game.currentState.discardStack.size)
        assertFails { root.playerActionService.drawWagonCard(1) }
    }

    /**
     * Tests the [PlayerActionService.drawDestinationCards] method
     */
    @Test
    fun testDrawDestinationCards() {
        fun PlayerActionService.assertDrawDestinationSuccess(cards: List<Int>) {
            val prevState = root.game.currentState
            drawDestinationCards(cards)
            val newState = root.game.currentState
            assertEquals(max(prevState.destinationCards.size - 3, 0), newState.destinationCards.size)
            val used = prevState.destinationCards.run { subList(max(0, size - 3), size) }
            assertEquals(max(0, prevState.destinationCards.size - 3), newState.destinationCards.size)
            val usedCards = cards.map(used::get)
            val addedToPlayer = prevState.players[prevState.currentPlayerIndex].destinationCards + usedCards
            assertEquals(newState.players[prevState.currentPlayerIndex].destinationCards, addedToPlayer)
        }
        fun PlayerActionService.assertDrawDestinationFail(cards: List<Int>) {
            val prevState = root.game.currentState
            assertFails { drawDestinationCards(cards) }
            assertSame(root.game.currentState, prevState)

        }
        val cities = constructGraph()
        val citiesByName = cities.associateBy { it.name }
        val destinationCards = destinationPool(citiesByName).subList(0, 7)
        val state = State(
            destinationCards,
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "asdsa", emptyList(), emptyList(), 40, emptyList(), false),
                Player(0, "gjij", emptyList(), emptyList(), 40, emptyList(), false),
            ),
            cities = cities
        )
        val root = RootService()
        root.game = Game(state)
        root.playerActionService.assertDrawDestinationFail(listOf(1, 2, 3, 4))
        root.playerActionService.assertDrawDestinationFail(listOf())
        root.playerActionService.assertDrawDestinationFail(listOf(1, 4))
        root.playerActionService.assertDrawDestinationFail(listOf(1, 1))
        root.playerActionService.assertDrawDestinationFail(listOf(-1, 1))
        root.playerActionService.assertDrawDestinationFail(listOf(-1))
        root.playerActionService.assertDrawDestinationFail(listOf(1, 3))
        root.playerActionService.assertDrawDestinationSuccess(listOf(1, 2))
        root.playerActionService.assertDrawDestinationSuccess(listOf(0))
        root.playerActionService.assertDrawDestinationFail(listOf(2))
        root.playerActionService.assertDrawDestinationFail(listOf(1, 3))
        root.playerActionService.assertDrawDestinationFail(listOf())
        root.playerActionService.assertDrawDestinationSuccess(listOf(0))
        root.playerActionService.assertDrawDestinationFail(listOf(0))
    }
}