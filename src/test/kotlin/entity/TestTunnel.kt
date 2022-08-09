package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Tunnel] class
 */
class TestTunnel {
    /**
     * Tests [Tunnel.equals] and [Tunnel.hashCode]
     */
    @Test
    fun testEquals() {
        val city1 = City(name = "", emptyList())
        val city2 = City(name = "asdad", emptyList())
        val city3 = City(name = "asda1312313", emptyList())
        val player = Player(1, "asds", emptyList(), emptyList(), 42)
        val tunnel1 = Tunnel(10, Color.BLACK, city1 to city2, null)
        testEqualsHash(tunnel1, tunnel1)
        val tunnel2 = Tunnel(10, Color.BLACK, city1 to city2, null)
        testEqualsHash(tunnel1, tunnel2)
        val ferry1 = Ferry(ferries = 3, 10, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(tunnel1, ferry1)
        val route1 = Route(10, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(tunnel1, route1)
        val tunnel3 = Tunnel(11, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(tunnel1, tunnel3)
        val tunnel4 = Tunnel(10, Color.RED, city1 to city2, null)
        testNotEqualsHash(tunnel1, tunnel4)
        val tunnel5 = Tunnel(10, Color.BLACK, city1 to city3, null)
        testNotEqualsHash(tunnel1, tunnel5)
        val tunnel6 = Tunnel( 10, Color.BLACK, city1 to city2, player)
        testNotEqualsHash(tunnel1, tunnel6)
    }

    /**
     * Tests [Tunnel.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val tunnel = Tunnel(13, Color.Joker, city1 to city1, null)
        assertEquals(
            "Tunnel(City(name=asdad, routes=[]) to City(name=asdad, routes=[])," +
                    " length=13, color=Joker, claimed by=null)",
            tunnel.toString()
        )
    }

    /**
     * Tests [Tunnel.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val tunnel = Tunnel(5, Color.BLACK, city1 to city1, null)
        assertEquals(5, tunnel.completeLength)
    }

    /**
     * Tests [Tunnel.asClaimedBy]
     */
    @Test
    fun testAsClaimedBy() {
        val city1 = City(name = "asdad", emptyList())
        val player = Player(3,  "wsda", emptyList(), emptyList(),4 )
        val tunnel = Tunnel(5, Color.BLACK, city1 to city1, null).asClaimedBy(player)
        val tunnel2 = Tunnel(5, Color.BLACK, city1 to city1, player)
        assertEquals(tunnel, tunnel2)
    }
}