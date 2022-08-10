package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Route] class
 */
class TestRoute {
    /**
     * Tests [Route.equals] and [Route.hashCode]
     */
    @Test
    fun testEquals() {
        val city1 = City(name = "", emptyList())
        val city2 = City(name = "asdad", emptyList())
        val city3 = City(name = "asda1312313", emptyList())
        val route1 = Route(10, listOf(Color.BLACK), city1 to city2)
        testEqualsHash(route1, route1)
        val route2 = Route(10, listOf(Color.BLACK), city1 to city2)
        testEqualsHash(route1, route2)
        val ferry1 = Ferry(ferries = 3, 10, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(route1, ferry1)
        val tunnel1 = Tunnel(10, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(route1, tunnel1)
        val route3 = Route(11, listOf(Color.BLACK), city1 to city2)
        testNotEqualsHash(route1, route3)
        val route4 = Route(10, listOf(Color.RED), city1 to city2)
        testNotEqualsHash(route1, route4)
        val route5 = Route(10, listOf(Color.BLACK), city1 to city3)
        testNotEqualsHash(route1, route5)
    }

    /**
     * Tests [Route.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Route(13, listOf(Color.Joker), city1 to city1)
        assertEquals(
            "Route(City(name=asdad, routes=[]) to City(name=asdad, routes=[])," +
                    " length=13, color=[Joker])",
            ferry.toString()
        )
    }

    /**
     * Tests [Route.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val route = Route(5, listOf(Color.BLACK), city1 to city1)
        assertEquals(5, route.completeLength)
    }
}