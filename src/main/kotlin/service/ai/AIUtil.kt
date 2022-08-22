package service.ai

import entity.*
import java.util.ArrayList
import java.util.HashMap
import kotlin.math.min

/**
 * Counts the occurrence of [WagonCard] of each color and saves the count at the [entity.Color.ordinal] index
 */
fun List<WagonCard>.counts(): IntArray {
    val counts = IntArray(9) { 0 }
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

private val wagonCardMoves = ArrayList<AIMove.DrawWagonCard>(3).apply {
    add(AIMove.DrawWagonCard(5, 5))
    for (i in 0..4) {
        add(AIMove.DrawWagonCard(i, 5))
        add(AIMove.DrawWagonCard(5, i))
        for (j in i..4) {
            add(AIMove.DrawWagonCard(i, j))
        }
    }
}
/**
 * computes draw wagon card moves with unique effects
 */
fun uniqueDrawWagonCard(currentState: State): List<AIMove.DrawWagonCard> {
    val canDraw = currentState.run { wagonCardsStack.size + discardStack.size } >= 2
    val drawWagonCards = if (canDraw) {
        wagonCardMoves
    } else {
        return emptyList()
    }
    val moves: HashMap<Long, AIMove.DrawWagonCard> = HashMap(20)
    val countArray = IntArray(9) { 0 }
    val drawStack = currentState.wagonCardsStack
    val openCards = currentState.openCards

    if (drawStack.size < 2) {
        return drawWagonCards
    }

    for (move in drawWagonCards) {
        val firstDrawCard = drawStack[drawStack.size - 1]
        val secondDrawCard = drawStack[drawStack.size - 2]

        for (card in openCards) {
            countArray[card.color.ordinal] += 1
        }

        var firstCard: Color
        var secondCard: Color

        if (move.firstDraw in 0..4) {
            firstCard = openCards[move.firstDraw].color
            countArray[firstCard.ordinal] -= 1
            countArray[firstDrawCard.color.ordinal] += 1
        } else {
            firstCard = firstDrawCard.color
        }
        if (move.secondDraw in 0..4) {
            secondCard = if (move.secondDraw == move.firstDraw) {
                firstDrawCard.color
            } else {
                openCards[move.secondDraw].color
            }
            countArray[secondCard.ordinal] -= 1
            countArray[secondDrawCard.color.ordinal] += 1
        } else {
            secondCard = secondDrawCard.color
        }

        var hash: Long = 0
        var factor: Long = 1
        for (i in 0 until 9) {
            hash += countArray[i] * factor
            factor *= 9
            countArray[i] = 0
        }
        if (firstCard.ordinal < secondCard.ordinal) {
            val tmp = firstCard
            firstCard = secondCard
            secondCard = tmp
        }
        hash = hash.shl(7).or((firstCard.ordinal * 9 + secondCard.ordinal).toLong())
        if (!moves.containsKey(hash)) {
            moves[hash] = move
        }
    }
    return moves.values.toList()
}