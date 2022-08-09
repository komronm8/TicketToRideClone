package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [WagonCard] class
 */
class TestWagonCard {
    /**
     * Tests [WagonCard.equals] and [WagonCard.hashCode]
     */
    @Test
    fun testEquals() {
        val card1 = WagonCard(color = Color.BLUE)
        testEqualsHash(card1, card1)
        val card2 = card1.copy()
        testEqualsHash(card1, card2)
        val card3 = card1.copy(color = Color.BLACK)
        testNotEqualsHash(card1, card3)
    }

    /**
     * Tests [WagonCard.toString]
     */
    @Test
    fun testToString() {
        val card = WagonCard(Color.BLUE)
        assertEquals(
            "WagonCard(color=BLUE)",
            card.toString()
        )
    }
}