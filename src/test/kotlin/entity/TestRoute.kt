package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Route] class
 */
class TestRoute {
    /**
     * Tests [Route.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Route(13, Color.JOKER, city1 to city1, 42)
        assertEquals(
            "Route(City(name = asdad) to City(name = asdad), length=13, color=JOKER, sibling: (), id: 42)",
            ferry.toString()
        )
    }

    /**
     * Tests [Route.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val route = Route(5, Color.BLACK, city1 to city1, 42)
        assertEquals(5, route.completeLength)
    }
}