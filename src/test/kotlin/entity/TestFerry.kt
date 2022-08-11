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
        val ferry1 = Ferry(ferries = 3, 10, listOf(Color.BLACK), city1 to city2)
        testEqualsHash(ferry1, ferry1)
        val ferry2 = Ferry(ferries = 3, 10, listOf(Color.BLACK), city1 to city2)
        testEqualsHash(ferry1, ferry2)
        val route1 = Route(10, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(ferry1, route1)
        val ferry3 = Ferry(ferries = 44, 10, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(ferry1, ferry3)
        val ferry4 = Ferry(ferries = 3, 11, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(ferry1, ferry4)
        val ferry5 = Ferry(ferries = 3, 10, listOf(Color.RED), city1 to city2)
        testNotEqualsHash(ferry1, ferry5)
        val ferry6 = Ferry(ferries = 3, 10, listOf(Color.BLACK), city1 to city3)
        testNotEqualsHash(ferry1, ferry6)
    }

    /**
     * Tests [Ferry.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Ferry(3, 13, listOf(Color.Joker), city1 to city1)
        assertEquals(
            "Ferry(City(name=asdad, routes=[]) to City(name=asdad, routes=[])," +
            " length=13, color=[Joker], ferries: 3)",
            ferry.toString()
        )
    }

    /**
     * Tests [Ferry.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Ferry(3, 5, listOf(Color.BLACK), city1 to city1)
        assertEquals(8, ferry.completeLength)
    }
}