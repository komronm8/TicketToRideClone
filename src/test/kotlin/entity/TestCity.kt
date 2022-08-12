package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [City] class
 */
class TestCity {
    /**
     * Tests [City.toString]
     */
    @Test
    fun testToString() {
        val city = City("Dortmund", emptyList())
        assertEquals("City(name = Dortmund)", city.toString())
    }
}