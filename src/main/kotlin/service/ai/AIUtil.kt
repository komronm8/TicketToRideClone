package service.ai

import entity.*
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * Counts the occurrence of [WagonCard] of each color and saves the count at the [entity.Color.ordinal] index
 */
fun List<WagonCard>.counts(counts: IntArray = IntArray(9) { 0 }): IntArray {
    for (card in this) counts[card.color.ordinal] += 1
    return counts
}

private val destinationIndices3 =
    listOf(listOf(0), listOf(1), listOf(2), listOf(0, 1), listOf(0, 2), listOf(1, 2), listOf(0, 1, 2))
        .map(AIMove::DrawDestinationCard)
private val destinationIndices2 =
    listOf(listOf(0), listOf(1), listOf(0, 1))
        .map(AIMove::DrawDestinationCard)
private val destinationIndices1 =
    listOf(listOf(0))
        .map(AIMove::DrawDestinationCard)

/**
computes all valid destination indices for the current state in regard to the amount of cards left
in [State.destinationCards]
 */
fun State.destinationIndices(): List<AIMove.DrawDestinationCard> {
    return when (min(destinationCards.size, 3)) {
        0 -> emptyList()
        1 -> destinationIndices1
        2 -> destinationIndices2
        3 -> destinationIndices3
        else -> throw IllegalStateException("unreachale")
    }
}

private val wagonCardsMoves = ArrayList<AIMove.DrawWagonCard>(21).apply {
    for (i in 0..4) {
        for (j in (i + 1)..4) {
            add(AIMove.DrawWagonCard(i, j))
        }
    }
    for (i in 0..4) {
        add(AIMove.DrawWagonCard(5, i))
    }
    for (i in 0..4) {
        add(AIMove.DrawWagonCard(i, 5))
    }
    add(AIMove.DrawWagonCard(5, 5))
    require(size == 21)
}

/**
 * computes draw wagon card moves with unique effects
 */
fun uniqueDrawWagonCard(currentState: State): List<AIMove.DrawWagonCard> {
    fun colorIndex(color1: Color, color2: Color): Int {
        val minColor = min(color1.ordinal, color2.ordinal)
        val maxColor = max(color1.ordinal, color2.ordinal)
        val fromEnd = 8 - minColor
        return (fromEnd * (fromEnd + 1)) / 2 + (maxColor - minColor)
    }
    val canDraw = currentState.run { wagonCardsStack.size + discardStack.size } >= 2
    if (!canDraw) {
        return emptyList()
    }
    val uniqueCards = ArrayList<AIMove.DrawWagonCard>(21)
    var seenColor = 0L

    val openCards = currentState.openCards
    val firstDraw = currentState.wagonCardsStack.lastOrNull()
    val secondDraw = currentState.wagonCardsStack.run { getOrNull(size - 2) }
    for (i in 0 until 10) {
        val draw = wagonCardsMoves[i]
        val index = colorIndex(openCards[draw.firstDraw].color, openCards[draw.secondDraw].color)
        if (seenColor.shr(index).and(1) == 0L) {
            uniqueCards.add(draw)
            seenColor = seenColor.or(1L.shl(index))
        }
    }
    if (firstDraw == null) {
        uniqueCards.addAll(wagonCardsMoves.subList(10, 21))
        return uniqueCards
    }
    for (i in 10 until 15) {
        val draw = wagonCardsMoves[i]
        val index = colorIndex(firstDraw.color, openCards[draw.secondDraw].color)
        if (seenColor.shr(index).and(1) == 0L) {
            uniqueCards.add(draw)
            seenColor = seenColor.or(1L.shl(index))
        }
    }
    if (secondDraw == null) {
        uniqueCards.addAll(wagonCardsMoves.subList(15, 21))
        return uniqueCards
    }
    for (i in 15 until 20) {
        val draw = wagonCardsMoves[i]
        val index = colorIndex(openCards[draw.firstDraw].color, secondDraw.color)
        if (seenColor.shr(index).and(1) == 0L) {
            uniqueCards.add(draw)
            seenColor = seenColor.or(1L.shl(index))
        }
    }
    run {
        val draw = wagonCardsMoves[20]
        val index = colorIndex(firstDraw.color, secondDraw.color)
        if (seenColor.shr(index).and(1) == 0L) {
            uniqueCards.add(draw)
            seenColor = seenColor.or(1L.shl(index))
        }
    }
    return uniqueCards
}