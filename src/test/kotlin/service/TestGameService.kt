package entity.service

import entity.*
import service.RootService
import service.constructGraph
import view.Refreshable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests the [service.GameService] class
 */
class TestGameService {
    /**
     * Tests the [service.GameService.nextPlayer] method
     */
    @Test
    fun testNextPlayer() {
        val player1 = Player(
            0, name = "asdas", emptyList(), List(2) { WagonCard(Color.YELLOW) }, trainCarsAmount = 2, isRemote = false
        )
        val state = State(
            emptyList(), List(5) { WagonCard(Color.JOKER) },
            List(20) { WagonCard(Color.JOKER) },
            emptyList(),
            listOf(
                player1,
                Player(0, "axdasd", emptyList(), emptyList(), isRemote = false)
            ),
            cities = constructGraph()
        )
        val cities = state.cities.associateBy { it.name }
        val root = RootService().apply { game = Game(state) }
        val hasWon = object: Refreshable {
            var winner: Player? = null
            override fun refreshAfterEndGame(winner: Player) {
                this.winner = winner
            }
        }
        root.addRefreshable(hasWon)
        val oul = checkNotNull(cities["Oulu"])
        val kaj = checkNotNull(cities["Kajaani"])
        root.playerActionService.claimRoute(oul.findRoute(kaj), player1.wagonCards)
        assertNull(hasWon.winner)
        assertEquals(player1.name, root.game.currentState.endPlayer?.name)
        assertEquals(1, root.game.currentState.currentPlayerIndex)
        assertEquals(GameState.DEFAULT, root.game.gameState)
        root.playerActionService.drawWagonCard(0)
        root.playerActionService.drawWagonCard(0)
        assertNull(hasWon.winner)
        assertEquals(player1.name, root.game.currentState.endPlayer?.name)
        assertEquals(0, root.game.currentState.currentPlayerIndex)
        assertEquals(GameState.DEFAULT, root.game.gameState)
        root.playerActionService.drawWagonCard(0)
        assertNull(hasWon.winner)
        root.playerActionService.drawWagonCard(0)
        assertNotNull(hasWon.winner)
    }
}