package entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests the [City] class
 */
class TestCity {
    /**
     * Tests [City.equals] and [City.hashCode]
     */
    @Test
    fun testEquals() {
        val city1 = City("sdjsifj", emptyList())
        assertEquals(city1, city1)
        val city2 = city1.copy(routes = listOf(Route(3, Color.BLACK, city1 to city1)))
        assertNotEquals(city1, city2)
        val city3 = city1.copy(name = "<fh<fuv")
        assertNotEquals(city1, city3)
    }

    /**
     * Tests [City.toString]
     */
    @Test
    fun testToString() {
        val city = City("Dortmund", emptyList())
        assertEquals("City(name=Dortmund, routes=[])", city.toString())
    }
}