package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Ferry] class
 */
class TestFerry {
    /**
     * Tests [Ferry.toString]
     */
    @Test
    fun testToString() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Ferry(3, 13, Color.JOKER, city1 to city1, 43,  Route(3, Color.BLUE, city1 to city1, 42))
        assertEquals(
            "Ferry(City(name = asdad) to City(name = asdad), " +
                    "length=13, color=JOKER, sibling: Route(length = 3, color = BLUE), ferries: 3)",
            ferry.toString()
        )
    }

    /**
     * Tests [Ferry.completeLength]
     */
    @Test
    fun testCompleteLength() {
        val city1 = City(name = "asdad", emptyList())
        val ferry = Ferry(3, 5, Color.BLACK, city1 to city1, 42)
        assertEquals(8, ferry.completeLength)
    }
}