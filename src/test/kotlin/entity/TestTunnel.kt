package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Tunnel] class
 */
class TestTunnel {
    /**
     * Tests [Tunnel.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val tunnel = Tunnel(13, Color.JOKER, city1 to city1, 42)
        assertEquals(
            "Tunnel(City(name = asdad) to City(name = asdad), length=13, color=JOKER, sibling: ())",
            tunnel.toString()
        )
    }

    /**
     * Tests [Tunnel.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val tunnel = Tunnel(5, Color.BLACK, city1 to city1,42)
        assertEquals(5, tunnel.completeLength)
    }
}