package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Player] class
 */
class TestPlayer {
    /**
     * Tests [Player.equals] and [Player.hashCode]
     */
    @Test
    fun testEquals() {
        val city1 = City("aasd", emptyList())
        val destCard = DestinationCard(4, city1 to city1)
        val player1 = Player(1, "abc", emptyList(), emptyList(), 13, emptyList(), false)
        testEqualsHash(player1, player1)
        val player2 = player1.copy()
        testEqualsHash(player1, player2)
        val player3 = player1.copy(points = 3)
        testNotEqualsHash(player1, player3)
        val player4 = Player(1, "abcg", emptyList(), emptyList(), 13, emptyList(), false)
        testNotEqualsHash(player1, player4)
        val player5 = player1.copy(destinationCards = listOf(destCard))
        testNotEqualsHash(player1, player5)
        val player6 = player1.copy(destinationCards = listOf(destCard.copy(points = 42)))
        testNotEqualsHash(player1, player6)
        val player7 = player1.copy(wagonCards = listOf(WagonCard(Color.BLACK)))
        testNotEqualsHash(player1, player7)
        val player8 = player1.copy(trainCarsAmount = 1313)
        testNotEqualsHash(player1, player8)
        val player9 =
            player1.copy(claimedRoutes = listOf(Route(12, Color.BLACK, city1 to city1)))
        testNotEqualsHash(player1, player9)
    }

    /**
     * Tests [Player.toString]
     */
    @Test
    fun testToString() {
        val player = Player(123, "asasd", emptyList(), listOf(WagonCard(Color.BLACK)), 3,  emptyList(), false)
        assertEquals(
            "Player(points=123, name='asasd', destinationCards=[], " +
                    "wagonCards=[WagonCard(color=BLACK)], trainCarsAmount=3, claimedRoutes=[])",
            player.toString()
        )
    }
}