package entity.service.ai

import entity.*
import service.ai.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests the methods in service.ai.AIUtil
 */
class TestAIUtil {
    /**
     * Tests the [service.ai.counts] method
     */
    @Test
    fun testCounts() {
        val colors = List(3) { Color.JOKER } +
                List(14) { Color.YELLOW } +
                List(1) { Color.BLACK }
        val cards = colors.map(::WagonCard).shuffled()
        val counts = cards.counts()
        assertEquals(14, counts[Color.YELLOW.ordinal])
        assertEquals(3, counts[Color.JOKER.ordinal])
        assertEquals(1, counts[Color.BLACK.ordinal])
        counts[Color.YELLOW.ordinal] = 0
        counts[Color.BLACK.ordinal] = 0
        counts[Color.JOKER.ordinal] = 0
        assert(counts.all { it == 0 })
    }

    /**
     * Tests the [service.ai.uniqueDrawWagonCard] method
     */
    @Test
    fun testUniqueCards() {
        val state1 = State(
            emptyList(),
            List(5) { WagonCard(Color.GREEN) },
            List(2) { WagonCard(Color.GREEN) },
            emptyList(), emptyList(), cities = emptyList()
        )
        assertEquals(1, uniqueDrawWagonCard(state1).size)
        val state2 = state1.copy(
            openCards = state1.openCards.toMutableList().also { it[2] = WagonCard(Color.YELLOW) }
        )
        val res2 = uniqueDrawWagonCard(state2)
        assertEquals(2, res2.size)
        if (res2[0].firstDraw == 2) {
            assertNotEquals(2, res2[1].firstDraw)
            assertNotEquals(2, res2[1].secondDraw)
        } else {
            assertNotEquals(2, res2[0].secondDraw)
            assert(res2[1].firstDraw == 2 || res2[1].secondDraw == 2)
        }
        val state3 = state1.copy(
            openCards = state1.openCards.toMutableList().also {
                it[2] = WagonCard(Color.YELLOW)
                it[4] = WagonCard(Color.YELLOW)
            }
        )
        val res3 = uniqueDrawWagonCard(state3)
        assertEquals(3, res3.size)
        val state4 = state1.copy(wagonCardsStack = listOf(Color.GREEN, Color.YELLOW).map(::WagonCard))
        assertEquals(2, uniqueDrawWagonCard(state4).size)
    }

    /**
     * Tests the [service.ai.destinationIndices]
     */
    @Test
    fun testDestinationIndices() {
        val city = City("cöuhsUFHWRUIVRWV", emptyList())
        val destinationCard = DestinationCard(-1, city to city)
        val state3 = State(
            List(3) { destinationCard },
            emptyList(), emptyList(), emptyList(), emptyList(), cities = emptyList(),
        )
        val expected3 =
            listOf(listOf(0), listOf(1), listOf(2), listOf(0, 1), listOf(0, 2), listOf(1, 2), listOf(0, 1, 2))
            .map(AIMove::DrawDestinationCard)
        val actual3 = state3.destinationIndices()
        assertEquals(expected3.toSet(), actual3.toSet())
        val state2 = state3.copy(List(2) { destinationCard },)
        val expected2 =
            listOf(listOf(0), listOf(1), listOf(0, 1))
                .map(AIMove::DrawDestinationCard)
        val actual2= state2.destinationIndices()
        assertEquals(expected2.toSet(), actual2.toSet())
        val state1 = state2.copy(List(1) { destinationCard },)
        val expected1 =
            listOf(listOf(0))
                .map(AIMove::DrawDestinationCard)
        val actual1 = state1.destinationIndices()
        assertEquals(expected1.toSet(), actual1.toSet())
        val state0 = state2.copy(emptyList())
        val expected0 = emptyList<AIMove.DrawDestinationCard>()
        val actual0 = state0.destinationIndices()
        assertEquals(expected0.toSet(), actual0.toSet())
    }
}
