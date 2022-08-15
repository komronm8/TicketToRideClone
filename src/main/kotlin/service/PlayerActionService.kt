package service

import entity.*
import kotlin.math.max
import kotlin.math.min

class PlayerActionService(val root: RootService) {
    private var state: State
        get() = root.game.currentState
        set(value) {
            root.insert(value)
        }

    /**
     * Splits the receiving list at [atIndex]. Everything before [atIndex] will be put into the first list,
     * every item after and at [atIndex] will be put into the second list
     */
    private fun <T> List<T>.splitAt(atIndex: Int): Pair<List<T>, List<T>> {
        check(atIndex in indices)
        return subList(0, atIndex) to subList(atIndex, size)
    }

    private inline fun State.updateCurrentPlayer(update: Player.() -> Player): List<Player> {
        return players.toMutableList().also {
            it[currentPlayerIndex] = it[currentPlayerIndex].update()
        }
    }

    private fun Route.pointValue(): Int = when (this.completeLength) {
        1 -> 1
        2 -> 2
        3 -> 4
        4 -> 7
        5 -> 10
        6 -> 15
        else -> 27
    }

    fun drawDestinationCards(cards: List<Int>) {
        when (root.game.gameState) {
            GameState.DEFAULT -> {}
            else -> throw IllegalStateException("illegal state for draw destination card")
        }
        val drawAmount = min(state.destinationCards.size, 3)
        assert(drawAmount >= cards.size)
        assert(cards.isNotEmpty())
        cards.forEach { it in 0 until drawAmount }
        cards.forEachIndexed { index, i -> cards.forEachIndexed { index2, i2 ->
            assert(i != i2 || index == index2)
        } }
        val (newDestinationStack, drawnCards) =
            state.destinationCards.splitAt(state.destinationCards.size - drawAmount)
        val newDestinationCards = state.currentPlayer.destinationCards + cards.map(drawnCards::get)
        val newPlayer = state.updateCurrentPlayer { copy(destinationCards = newDestinationCards) }
        state = state.copy(destinationCards = newDestinationStack, players = newPlayer)
        root.gameService.nextPlayer()
    }

    fun drawWagonCard(cardIndex: Int) {
        when (root.game.gameState) {
            GameState.DREW_WAGON_CARD -> {}
            GameState.DEFAULT -> {
                check(state.discardStack.size + state.wagonCardsStack.size >= 2)
            }

            else -> throw IllegalStateException("Cannot draw wagon card: Wrong game state")
        }
        var newDrawStack = state.wagonCardsStack.toMutableList()
        var newDiscardStack = state.discardStack
        var openCards = state.openCards
        if (newDrawStack.isEmpty()) {
            val oldDrawStack = newDrawStack
            newDrawStack = newDiscardStack.toMutableList().apply { shuffle() }
            newDiscardStack = oldDrawStack
        }
        var insertCard = newDrawStack.removeLast()
        if (cardIndex in openCards.indices) {
            openCards = openCards.toMutableList().also {
                val exchanged = it[cardIndex]
                it[cardIndex] = insertCard
                insertCard = exchanged
            }
        }
        val newPlayers = state.players.toMutableList().also {
            it[state.currentPlayerIndex] = it[state.currentPlayerIndex].run {
                copy(wagonCards = wagonCards + insertCard)
            }
        }
        val newState = state.copy(
            discardStack = newDiscardStack,
            wagonCardsStack = newDrawStack,
            players = newPlayers,
            openCards = openCards
        )
        state = newState
        when (root.game.gameState) {
            GameState.DEFAULT -> {
                root.game.gameState = GameState.DREW_WAGON_CARD
            }

            GameState.DREW_WAGON_CARD -> {
                root.undo()
                root.gameService.nextPlayer()
            }

            else -> {
                throw IllegalStateException("unreachable")
            }
        }
    }

    fun claimRoute(route: Route, usedCards: List<WagonCard>) {
        when (root.game.gameState) {
            GameState.DEFAULT -> {}
            else -> throw IllegalStateException("Illegal state for claim route")
        }
        assert(state.currentPlayer.trainCarsAmount >= route.completeLength)
        val doubleRoute = state.players.size > 2
        state.players.forEach { player ->
            player.claimedRoutes.forEach {
                if (it === route)
                    throw IllegalStateException("Route already claimed")
                if (it.sibling === route && (!doubleRoute || player === state.currentPlayer))
                    throw IllegalStateException("Cannot claim double route")
            }
        }
        val currentPlayer = state.currentPlayer
        assert(usedCards.all { card -> currentPlayer.wagonCards.any { it === card } })
        usedCards.forEachIndexed { index, wagonCard ->
            usedCards.forEachIndexed { index2, wagonCard2 ->
                assert(wagonCard !== wagonCard2 || index == index2)
            }
        }
        assert(canClaimRoute(route, usedCards))
        val newPlayerCard = currentPlayer.wagonCards.filter { card -> usedCards.none { it === card } }
        if (route is Tunnel && state.wagonCardsStack.size + state.discardStack.size > 0) {
            var newDiscardStack = state.discardStack
            var newDrawStack = state.wagonCardsStack
            if (state.wagonCardsStack.size < 3) {
                val remainingOnDraw = state.wagonCardsStack.size
                newDrawStack = ArrayList<WagonCard>(state.discardStack.size + remainingOnDraw).apply {
                    addAll(state.discardStack)
                    shuffle()
                    addAll(state.wagonCardsStack)
                }
                newDiscardStack = emptyList()
            }
            val newPlayers = state.updateCurrentPlayer {
                copy(wagonCards = newPlayerCard)
            }
            newDiscardStack = newDiscardStack + usedCards
            state = state.copy(
                wagonCardsStack = newDrawStack,
                discardStack = newDiscardStack,
                players = newPlayers,
            )
            root.game.gameState = GameState.AFTER_CLAIM_TUNNEL
        } else {
            val newPlayer = state.updateCurrentPlayer {
                copy(
                    wagonCards = newPlayerCard,
                    points = points + route.pointValue(),
                    claimedRoutes = claimedRoutes + route,
                    trainCarsAmount = trainCarsAmount - route.completeLength
                )
            }
            val newDiscardStack = state.discardStack + usedCards
            state = state.copy(discardStack = newDiscardStack, players = newPlayer)
            root.gameService.nextPlayer()
        }
    }

    private fun canClaimRoute(route: Route, cards: List<WagonCard>): Boolean {
        val color = route.color
        if (color != Color.JOKER) {
            var locomotiveCount = 0
            var colorCardCount = 0
            var otherCardCount = 0
            for (card in cards) {
                when (card.color) {
                    Color.JOKER -> locomotiveCount += 1
                    color -> colorCardCount += 1
                    else -> otherCardCount += 1
                }
            }
            return canClaimRoundWithCounts(route, locomotiveCount, colorCardCount, otherCardCount)
        } else {
            val jokerGuard = if (route !is Ferry) null else Color.JOKER
            var overallCount = 0
            var indexOfMax = 0
            val specificCounts = IntArray(Color.values().size)
            for (card in cards) {
                specificCounts[card.color.ordinal] += 1
                overallCount += 1
                if (specificCounts[card.color.ordinal] > specificCounts[indexOfMax] && card.color != jokerGuard)
                    indexOfMax = card.color.ordinal
            }
            val locomotiveCount: Int
            val colorCardCount: Int
            if (Color.JOKER.ordinal == indexOfMax) {
                colorCardCount = specificCounts[indexOfMax]
                locomotiveCount = 0
            } else {
                locomotiveCount = specificCounts[Color.JOKER.ordinal]
                colorCardCount = specificCounts[indexOfMax]
            }
            val otherCardCount = overallCount - locomotiveCount - colorCardCount
            return canClaimRoundWithCounts(route, locomotiveCount, colorCardCount, otherCardCount)
        }

    }

    private fun canClaimRoundWithCounts(
        route: Route,
        locomotiveCount: Int,
        colorCardCount: Int,
        otherCardCount: Int
    ): Boolean {
        var remLocomotiveCount = locomotiveCount
        var remOtherCardCount = otherCardCount
        var remaining = route.length
        var requiredLocomotives = if (route is Ferry) route.ferries else 0
        val canUseLocomotives = requiredLocomotives > 0
        val exchangeRate = when {
            route is Ferry -> 3
            route.isMurmanskLieksa() -> 4
            else -> 0
        }

        if (remLocomotiveCount < requiredLocomotives) {
            requiredLocomotives -= remLocomotiveCount
            remLocomotiveCount = 0
        } else {
            remLocomotiveCount -= requiredLocomotives
            requiredLocomotives = 0
        }
        remaining -= min(colorCardCount, route.length)
        //max(...) is used, in order to prevent otherCardCount becoming negative
        remOtherCardCount += max(0, colorCardCount - route.length)
        if (canUseLocomotives) {
            if (remLocomotiveCount > remaining) {
                return false
            } else {
                remaining -= remLocomotiveCount
                remLocomotiveCount = 0
            }
        }
        remaining += requiredLocomotives
        if (exchangeRate > 0) {
            remOtherCardCount += remLocomotiveCount
            remLocomotiveCount = 0
            remaining -= remOtherCardCount / exchangeRate
            if (remOtherCardCount % exchangeRate != 0 || remaining < 0) return false
            remOtherCardCount = 0
        }
        return remLocomotiveCount == 0 && remOtherCardCount == 0 && remaining == 0
    }

}