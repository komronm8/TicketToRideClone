package service

import entity.*
import view.Refreshable
import kotlin.math.max
import kotlin.math.min

/**
 * The service responsible for actions performed by the player
 */
class PlayerActionService(val root: RootService): AbstractRefreshingService() {
    private var state: State
        get() = root.game.currentState
        set(value) {
            root.insert(value)
        }

    private inline fun State.updateCurrentPlayer(update: Player.() -> Player): List<Player> {
        return players.toMutableList().also {
            it[currentPlayerIndex] = it[currentPlayerIndex].update()
        }
    }
    /**
     * Splits the receiving list at [atIndex]. Everything before [atIndex] will be put into the first list,
     * every item after and at [atIndex] will be put into the second list
     */
    private fun <T> List<T>.splitAt(atIndex: Int): Pair<List<T>, List<T>> {
        check(atIndex in 0..size)
        return subList(0, atIndex) to subList(atIndex, size)
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

    /**
     * Pops the top 3 cards of the destination draw stack, retains the cards specified by their index in [cards]
     * and puts the retained cards in the current player's hand cards, then advances to next player.
     *
     * [Game.gameState] must be [GameState.DEFAULT]
     *
     * @param cards The retained cards. Must contain at least one card, cannot contain duplicates, indices must be
     * in bounds of drawn card
     *
     * @throws IllegalStateException when conditions are not fulfilled
     */
    fun drawDestinationCards(cards: List<Int>) {
        when (root.game.gameState) {
            GameState.DEFAULT -> {}
            else -> throw IllegalStateException("illegal state for draw destination card")
        }
        val drawAmount = min(state.destinationCards.size, 3)
        println("$drawAmount/${cards.size}|$cards")
        check(drawAmount >= cards.size)
        check(cards.isNotEmpty())
        cards.forEach {check(it in 0 until drawAmount) }
        cards.forEachIndexed { index, i ->
            cards.forEachIndexed { index2, i2 ->
                check(i != i2 || index == index2)
            }
        }
        val (newDestinationStack, drawnCards) =
            state.destinationCards.splitAt(state.destinationCards.size - drawAmount)
        val newDestinationCards = state.currentPlayer.destinationCards + cards.map(drawnCards::get)
        val newPlayer = state.updateCurrentPlayer { copy(destinationCards = newDestinationCards) }
        state = state.copy(destinationCards = newDestinationStack, players = newPlayer)
        onAllRefreshables(Refreshable::refreshAfterDrawDestinationCards)
        root.gameService.nextPlayer()
    }

    /**
     * Draws a card from the [draw stack][State.wagonCardsStack] or the [open cards][State.openCards]
     * Advances to the next player if the current player has drawn twice
     *
     * Draws from the open cards when 0 ≤ [cardIndex] < 5, draws from the draw stack otherwise.
     * The [Game.gameState] can be [GameState.DEFAULT] or [GameState.DREW_WAGON_CARD].
     * If the original state was [GameState.DEFAULT] then the [draw stack][State.wagonCardsStack] and
     * [discard stack][State.discardStack] must have at least 2 cards combined
     *
     * Calls the [Refreshable.refreshAfterNextPlayer] regardless of state
     *
     */
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
        when (root.game.gameState) {
            GameState.DEFAULT -> {
                state = newState
                root.game.gameState = GameState.DREW_WAGON_CARD
            }

            GameState.DREW_WAGON_CARD -> {
                root.undo()
                state = newState
                root.game.gameState = GameState.DEFAULT
                root.gameService.nextPlayer()
            }

            else -> {
                throw IllegalStateException("unreachable")
            }
        }
        onAllRefreshables(Refreshable::refreshAfterDrawWagonCards)
    }

    /**
     * Claims the route for the player
     *
     * The [Game.gameState] must be [GameState.DEFAULT]. The [usedCards] must fit exactly to claim the route.
     * Must have sufficient [Player.trainCarsAmount]. If the route is a tunnel, then the required cards are placed on
     * top of the draw stack
     *
     * @param route the route to be claimed. Must either be unclaimed or a double route
     * @param usedCards the cards that are used to claim the route. Must be distinct and in possession of the player
     */
    fun claimRoute(route: Route, usedCards: List<WagonCard>) {
        when (root.game.gameState) {
            GameState.DEFAULT -> {}
            else -> throw IllegalStateException("Illegal state for claim route")
        }
        usedCards.forEachIndexed { index, wagonCard ->
            usedCards.forEachIndexed { index2, wagonCard2 ->
                check(wagonCard !== wagonCard2 || index == index2)
            }
        }
        validateClaimRoute(state.currentPlayer, route, usedCards, true)
        val newPlayerCard = state.currentPlayer.wagonCards.filter { card -> usedCards.none { it === card } }
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
            onAllRefreshables(Refreshable::refreshAfterClaimRoute)
            root.gameService.nextPlayer()
            return
        }
        onAllRefreshables(Refreshable::refreshAfterClaimRoute)
    }

    fun validateClaimRoute(currentPlayer: Player, route: Route, usedCards: List<WagonCard>, exhaustive: Boolean) {
        check(currentPlayer.trainCarsAmount >= route.completeLength)
        val doubleRoute = state.players.size > 2
        state.players.forEach { player ->
            player.claimedRoutes.forEach {
                if (it === route)
                    throw IllegalStateException("Route already claimed")
                if (it.sibling === route && (!doubleRoute || player === state.currentPlayer))
                    throw IllegalStateException("Cannot claim double route")
            }
        }
        check(usedCards.all { card -> currentPlayer.wagonCards.any { it === card } })
        check(canClaimRoute(route, usedCards, exhaustive))
    }

    /**
     * Finalises the  claiming of a tunnel.
     * @param route the tunnel
     * @param cards `null` if the player does not wish to pay, otherwise the given cards must be enough
     * to satisfy the required cards
     */
    fun afterClaimTunnel(route: Tunnel, cards: List<WagonCard>?) {
        cards?.also {
            check(cards.all { given -> state.currentPlayer.wagonCards.any { it === given } })
            cards.forEachIndexed { index1, card1 ->
                cards.forEachIndexed { index2, card2 ->
                    check(card1 !== card2 || index1 == index2)
                }
            }
        }
        val newState = root.game.currentState
        root.undo()
        val previousState = root.game.currentState
        val prevPlayerHand = previousState.currentPlayer.wagonCards
        val afterPlayerHand = newState.currentPlayer.wagonCards
        val handDiff = prevPlayerHand.count { oldCard -> afterPlayerHand.none { newCard -> oldCard === newCard } }
        val (newDiscard, usedCards) = newState.discardStack.run { splitAt(size - handDiff) }
        val (newDraw, requiredCards) = newState.wagonCardsStack.run { splitAt(max(0, size - 3)) }

        if (cards == null) {
            state = previousState.copy(
                discardStack = newDiscard + requiredCards,
                wagonCardsStack = newDraw
            )
            root.game.gameState = GameState.DEFAULT
            root.gameService.nextPlayer()
            return
        }
        if (usedCards.all { it.color == Color.JOKER }) {
            val locomotives = requiredCards.count { it.color == Color.JOKER }
            check(cards.all { it.color == Color.JOKER } && locomotives == cards.size)
        } else {
            assert(route.color != Color.JOKER) { "Tunnel should not be gray!" }
            val required = requiredCards.count { it.color == route.color || it.color == Color.JOKER }
            val given = cards.count { it.color == route.color || it.color == Color.JOKER }
            check(required == given)
            check(given == cards.size)
        }
        val newPlayerHand = newState.currentPlayer.wagonCards.filter { card -> cards.none { it === card } }
        val newPlayers = newState.updateCurrentPlayer {
            copy(
                points = points + route.pointValue(),
                trainCarsAmount = trainCarsAmount - route.completeLength,
                claimedRoutes = claimedRoutes + route,
                wagonCards = newPlayerHand
            )
        }
        state = newState.copy(
            discardStack = newState.discardStack + requiredCards,
            wagonCardsStack = newDraw,
            players = newPlayers
        )
        root.game.gameState = GameState.DEFAULT
        onAllRefreshables(Refreshable::refreshAfterAfterClaimTunnel)
        root.gameService.nextPlayer()
    }

    private fun canClaimRoute(route: Route, cards: List<WagonCard>, exhaustive: Boolean): Boolean {
        val jokerGuard = if (route is Ferry) Color.JOKER else null
        val counts = cards.groupBy { it.color }.mapValues { it.value.count() }
        val maxCountColor = counts.filter { it.key != jokerGuard }.maxByOrNull { it.value }?.key ?: Color.PURPLE

        val (locomotiveCount, colorCardCount) = if (route.color != Color.JOKER) {
            (counts[Color.JOKER] ?: 0) to (counts[route.color] ?: 0)
        } else if (maxCountColor == Color.JOKER) {
            0 to (counts[Color.JOKER] ?: 0)
        } else {
            (counts[Color.JOKER] ?: 0) to (counts[maxCountColor] ?: 0)
        }
        var otherCardCount = cards.size - locomotiveCount - colorCardCount
        when {
            route is Ferry -> {
                val requiredCount = if (colorCardCount >= route.length) {
                    otherCardCount += colorCardCount - route.length
                    0
                } else {
                    route.length - colorCardCount
                }
                val required = requiredCount + (route.ferries - locomotiveCount) - (otherCardCount / 3)
                return if (exhaustive)
                    required == 0 && otherCardCount % 3 == 0
                else
                    required <= 0
            }

            route is Tunnel -> {
                val required = route.length + -locomotiveCount - colorCardCount
                return if (exhaustive)
                    required == 0&& otherCardCount == 0
                else
                    required <= 0
            }

            route.isMurmanskLieksa() -> {
                val mixedBudget = otherCardCount + locomotiveCount
                val required = route.length - colorCardCount - (mixedBudget / 4)
                return if (exhaustive)
                    required == 0 && mixedBudget % 4 == 0
                else
                    required <= 0
            }

            else -> {
                val required = route.length - colorCardCount
                return if (exhaustive)
                    required == 0 && otherCardCount == 0 && locomotiveCount == 0
                else
                    required <= 0
            }
        }
    }

    /**
     * Reverts the entire last round
     */
    fun undo() {
        root.undo()
        root.game.gameState = GameState.DEFAULT
        onAllRefreshables(Refreshable::refreshAfterUndoRedo)
    }

    /**
     * Recovers the undone rounds
     */
    fun redo() {
        root.redo()
        root.game.gameState = GameState.DEFAULT
        onAllRefreshables(Refreshable::refreshAfterUndoRedo)
    }

}


