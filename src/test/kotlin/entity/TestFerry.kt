package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Ferry] class
 */
class TestFerry {
    /**
     * Tests [Ferry.equals] and [Ferry.toString]
     */
    @Test
    fun testEquals() {
        val city1 = City(name = "", emptyList())
        val city2 = City(name = "asdad", emptyList())
        val city3 = City(name = "asda1312313", emptyList())
        val player = Player(1, "asds", emptyList(), emptyList(), 42)
        val ferry1 = Ferry(ferries = 3, 10, Color.BLACK, city1 to city2, null)
        testEqualsHash(ferry1, ferry1)
        val ferry2 = Ferry(ferries = 3, 10, Color.BLACK, city1 to city2, null)
        testEqualsHash(ferry1, ferry2)
        val route1 = Route(10, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(ferry1, route1)
        val ferry3 = Ferry(ferries = 44, 10, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(ferry1, ferry3)
        val ferry4 = Ferry(ferries = 3, 11, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(ferry1, ferry4)
        val ferry5 = Ferry(ferries = 3, 10, Color.RED, city1 to city2, null)
        testNotEqualsHash(ferry1, ferry5)
        val ferry6 = Ferry(ferries = 3, 10, Color.BLACK, city1 to city3, null)
        testNotEqualsHash(ferry1, ferry6)
        val ferry7 = Ferry(ferries = 3, 10, Color.BLACK, city1 to city2, player)
        testNotEqualsHash(ferry1, ferry7)
    }

    /**
     * Tests [Ferry.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Ferry(3, 13, Color.Joker, city1 to city1, null)
        assertEquals(
            "Ferry(City(name=asdad, routes=[]) to City(name=asdad, routes=[])," +
            " length=13, color=Joker, claimed by=null, ferries: 3)",
            ferry.toString()
        )
    }

    /**
     * Tests [Ferry.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Ferry(3, 5, Color.BLACK, city1 to city1, null)
        assertEquals(8, ferry.completeLength)
    }

    /**
     * Tests [Ferry.asClaimedBy]
     */
    @Test
    fun testAsClaimedBy() {
        val city1 = City(name = "asdad", emptyList())
        val player = Player(3,  "wsda", emptyList(), emptyList(),4 )
        val ferry = Ferry(3, 5, Color.BLACK, city1 to city1, null).asClaimedBy(player)
        val ferry2 = Ferry(3, 5, Color.BLACK, city1 to city1, player)
        assertEquals(ferry, ferry2)
    }
}