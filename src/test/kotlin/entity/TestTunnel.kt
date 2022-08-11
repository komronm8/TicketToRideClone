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
        val tunnel1 = Tunnel(10, listOf(Color.BLACK), city1 to city2)
        testEqualsHash(tunnel1, tunnel1)
        val tunnel2 = Tunnel(10, listOf(Color.BLACK), city1 to city2)
        testEqualsHash(tunnel1, tunnel2)
        val ferry1 = Ferry(ferries = 3, 10, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(tunnel1, ferry1)
        val route1 = Route(10, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(tunnel1, route1)
        val tunnel3 = Tunnel(11, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(tunnel1, tunnel3)
        val tunnel4 = Tunnel(10, listOf(Color.RED), city1 to city2)
        testNotEqualsHash(tunnel1, tunnel4)
        val tunnel5 = Tunnel(10, listOf(Color.BLACK), city1 to city3)
        testNotEqualsHash(tunnel1, tunnel5)
    }

    /**
     * Tests [Tunnel.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val tunnel = Tunnel(13, listOf(Color.Joker), city1 to city1)
        assertEquals(
            "Tunnel(City(name=asdad, routes=[]) to City(name=asdad, routes=[])," +
                    " length=13, color=[Joker])",
            tunnel.toString()
        )
    }

    /**
     * Tests [Tunnel.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val tunnel = Tunnel(5, listOf(Color.BLACK), city1 to city1)
        assertEquals(5, tunnel.completeLength)
    }
}