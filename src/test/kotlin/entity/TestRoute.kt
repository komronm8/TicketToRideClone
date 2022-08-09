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
        val player = Player(1, "asds", emptyList(), emptyList(), 42)
        val route1 = Route(10, Color.BLACK, city1 to city2, null)
        testEqualsHash(route1, route1)
        val route2 = Route(10, Color.BLACK, city1 to city2, null)
        testEqualsHash(route1, route2)
        val ferry1 = Ferry(ferries = 3, 10, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(route1, ferry1)
        val tunnel1 = Tunnel(10, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(route1, tunnel1)
        val route3 = Route(11, Color.BLACK, city1 to city2, null)
        testNotEqualsHash(route1, route3)
        val route4 = Route(10, Color.RED, city1 to city2, null)
        testNotEqualsHash(route1, route4)
        val route5 = Route(10, Color.BLACK, city1 to city3, null)
        testNotEqualsHash(route1, route5)
        val route6 = Route( 10, Color.BLACK, city1 to city2, player)
        testNotEqualsHash(route1, route6)
    }

    /**
     * Tests [Route.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Route(13, Color.Joker, city1 to city1, null)
        assertEquals(
            "Route(City(name=asdad, routes=[]) to City(name=asdad, routes=[])," +
                    " length=13, color=Joker, claimed by=null)",
            ferry.toString()
        )
    }

    /**
     * Tests [Route.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val route = Route(5, Color.BLACK, city1 to city1, null)
        assertEquals(5, route.completeLength)
    }

    /**
     * Tests [Route.asClaimedBy]
     */
    @Test
    fun testAsClaimedBy() {
        val city1 = City(name = "asdad", emptyList())
        val player = Player(3,  "wsda", emptyList(), emptyList(),4 )
        val route = Route(5, Color.BLACK, city1 to city1, null).asClaimedBy(player)
        val route2 = Route(5, Color.BLACK, city1 to city1, player)
        assertEquals(route.javaClass, Route::class.java)
        assertEquals(route, route2)
    }
}