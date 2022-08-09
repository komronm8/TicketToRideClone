package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [DestinationCard] class
 */
class TestDestinationCard {
    /**
     * Tests [DestinationCard.equals] and [DestinationCard.hashCode]
     */
    @Test
    fun testEquals() {
        val city1 = City(name = "", emptyList())
        val city2 = City(name = "asdad", emptyList())
        val city3 = City(name = "asda1312313", emptyList())
        val dest1 = DestinationCard(42, city1 to city2)
        testEqualsHash(dest1, dest1)
        val dest2 = dest1.copy()
        testEqualsHash(dest1, dest2)
        val dest3 = dest1.copy(points = 3)
        testNotEqualsHash(dest1, dest3)
        val dest4 = dest1.copy(cities = city1 to city3)
        testNotEqualsHash(dest1, dest4)
    }

    /**
     * Tests [DestinationCard.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "abc", emptyList())
        val card = DestinationCard(42, city1 to city1)
        assertEquals(
            "DestinationCard(points=42, cities=(City(name=abc, routes=[]), City(name=abc, routes=[])))",
            card.toString()
        )
    }
}