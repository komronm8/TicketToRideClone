package service

import entity.*
import view.Refreshable
import kotlin.math.max
import kotlin.math.min

/**
 * The service responsible for actions performed by the player
 */
class PlayerActionService(val root: RootService) : AbstractRefreshingService() {
    /**
     * The failures that can upon claiming a route
     */
    sealed interface ClaimRouteFailure {
        /**
         * Specifies that the current player does not have enough train cars
         */
        object NotEnoughTrainCars : ClaimRouteFailure

        /**
         * Specifies that the sibling of the route to be claimed has already been claimed by the current player
         */
        object SiblingClaimedBySamePlayer : ClaimRouteFailure

        /**
         * Specifies that the route to be claimed has already been claimed
         */
        object RouteAlreadyClaimed : ClaimRouteFailure

        /**
         * Specifies that the sibling of the route to be claimed has already been claimed by another player and
         * that there are only two players
         */
        object SiblingClaimedByAnotherPlayer : ClaimRouteFailure

        /**
         * Specifies that the given cards do not suffice for claiming the route
         */
        object NotEnoughCards : ClaimRouteFailure

        /**
         * Specifies that too many cards were given for claiming the route
         */
        object TooManyCards : ClaimRouteFailure

        /**
         * Specifies that there were cards of illegal color in the given cards
         */
        object IllegalCards : ClaimRouteFailure
    }

    private inline val state: State
        get() = root.game.currentState

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
        check(drawAmount >= cards.size)
        check(cards.isNotEmpty())
        cards.forEach { check(it in 0 until drawAmount) }
        cards.forEachIndexed { index, i ->
            cards.forEachIndexed { index2, i2 ->
                check(i != i2 || index == index2)
            }
        }
        val (newDestinationStack, drawnCards) =
            state.destinationCards.splitAt(state.destinationCards.size - drawAmount)
        val newDestinationCards = state.currentPlayer.destinationCards + cards.map(drawnCards::get)
        val newPlayer = state.updateCurrentPlayer { copy(destinationCards = newDestinationCards) }
        root.insert(state.copy(destinationCards = newDestinationStack, players = newPlayer))
        onAllRefreshables(Refreshable::refreshAfterDrawDestinationCards)
        if (state.players.any { it.isRemote } && state.currentPlayer.name == root.network.client?.playerName) {
            root.network.sendDrawDestinationTicket(cards.map(drawnCards::get))
        }
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
        var newDrawStack = state.wagonCardsStack
        var newDiscardStack = state.discardStack
        var openCards = state.openCards
        if (newDrawStack.isEmpty()) {
            val oldDrawStack = newDrawStack
            newDrawStack = newDiscardStack.toMutableList().apply { shuffle() }
            newDiscardStack = oldDrawStack
        }
        var insertCard = newDrawStack.last()
        newDrawStack = newDrawStack.subList(0, newDrawStack.size - 1)
        if (cardIndex in openCards.indices && insertCard != openCards[cardIndex]) {
            openCards = openCards.toMutableList().also {
                val exchanged = it[cardIndex]
                it[cardIndex] = insertCard
                insertCard = exchanged
            }
        }
        val newPlayers = state.updateCurrentPlayer {
            copy(wagonCards = wagonCards + insertCard)
        }
        val newState = state.copy(
            discardStack = newDiscardStack,
            wagonCardsStack = newDrawStack,
            players = newPlayers,
            openCards = openCards
        )
        when (root.game.gameState) {
            GameState.DEFAULT -> {
                root.insert(newState)
                root.game.gameState = GameState.DREW_WAGON_CARD
            }

            GameState.DREW_WAGON_CARD -> {
                root.undo()
                root.insert(newState)
                root.game.gameState = GameState.DEFAULT
                println("Reached sending")
                if (state.players.any { it.isRemote } && state.currentPlayer.name == root.network.client?.playerName) {
                    val prevState = root.game.run { states[currentStateIndex - 1] }
                    val shuffled = prevState.discardStack.isNotEmpty() && state.discardStack.isEmpty()
                    val newCardStack = if (shuffled) state.wagonCardsStack else null
                    val selected = state.currentPlayer.wagonCards.filter { card ->
                        prevState.currentPlayer.wagonCards.none { card === it }
                    }
                    root.network.sendDrawTrainCardMessage(selected, newCardStack)
                }
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
    fun claimRoute(route: Route, usedCards: List<WagonCard>): ClaimRouteFailure? {
        when (root.game.gameState) {
            GameState.DEFAULT -> {}
            else -> throw IllegalStateException("Illegal state for claim route")
        }
        usedCards.forEachIndexed { index, wagonCard ->
            usedCards.forEachIndexed { index2, wagonCard2 ->
                require(wagonCard !== wagonCard2 || index == index2)
            }
        }
        validateClaimRoute(state.currentPlayer, route, usedCards, true)?.also { return it }
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
            root.insert(
                state.copy(
                    wagonCardsStack = newDrawStack,
                    discardStack = newDiscardStack,
                    players = newPlayers,
                )
            )
            root.game.gameState = GameState.AFTER_CLAIM_TUNNEL
            onAllRefreshables { refreshAfterClaimRoute(route, usedCards) }
            return null
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
            root.insert(state.copy(discardStack = newDiscardStack, players = newPlayer))
            onAllRefreshables { refreshAfterClaimRoute(route, usedCards) }
            if (root.game.currentState.players.any { it.isRemote } && state.currentPlayer.name == root.network.client?.playerName) {
                root.network.sendClaimARounteMessage(route, null, usedCards, null)
            }
            root.gameService.nextPlayer()
            return null
        }
    }

    /**
     * Checks whether a route can be claimed with the given cards and train cards of the player
     * @param currentPlayer The player who attempts to claim the route
     * @param route the route to be claimed
     * @param usedCards the cards which are used to claim the route
     * @param exhaustive sets whether the [usedCards] must suffice exactly to claim the route
     */
    fun validateClaimRoute(
        currentPlayer: Player, route: Route,
        usedCards: List<WagonCard>, exhaustive: Boolean
    ): ClaimRouteFailure? {
        if (currentPlayer.trainCarsAmount < route.completeLength) {
            return ClaimRouteFailure.NotEnoughTrainCars
        }
        val doubleRoute = state.players.size > 2
        state.players.forEach { player ->
            player.claimedRoutes.forEach {
                if (it === route) {
                    return ClaimRouteFailure.RouteAlreadyClaimed
                }
                if (it.sibling === route && !doubleRoute) {
                    return ClaimRouteFailure.SiblingClaimedBySamePlayer
                }
                if (it.sibling === route && player === state.currentPlayer) {
                    return ClaimRouteFailure.SiblingClaimedByAnotherPlayer
                }
            }
        }
        require(usedCards.all { card -> currentPlayer.wagonCards.any { it === card } })
        return canClaimRoute(route, usedCards, exhaustive)
    }

    /**
     * Checks whether the route can be claimed with the cards given
     * @param route The route which should be claimed
     * @param cards The cards which are used to claim the route
     * @param exhaustive Sets whether the cards suffice exactly
     */
    fun canClaimRoute(route: Route, cards: List<WagonCard>, exhaustive: Boolean): ClaimRouteFailure? {
        val counts = IntArray(9) { 0 }
        for (card in cards) {
            counts[card.color.ordinal] += 1
        }
        val locomotiveCount: Int
        val colorCardCount: Int
        if (route.color != Color.JOKER) {
            locomotiveCount = counts[Color.JOKER.ordinal]
            colorCardCount = counts[route.color.ordinal]
        } else {
            val jokerGuard = if (route is Ferry) Color.JOKER else null
            var maxCountColor = Color.GREEN
            for (color in Color.values()) {
                if (color != jokerGuard && counts[color.ordinal] > counts[maxCountColor.ordinal]) {
                    maxCountColor = color
                }
            }
            if (maxCountColor == Color.JOKER) {
                locomotiveCount = 0
                colorCardCount = counts[maxCountColor.ordinal]
            } else {
                locomotiveCount = counts[Color.JOKER.ordinal]
                colorCardCount = counts[maxCountColor.ordinal]
            }
        }
        val otherCardCount = cards.size - locomotiveCount - colorCardCount
        return canClaimWithCounts(route, colorCardCount, locomotiveCount, otherCardCount, exhaustive)
    }

    /**
     * Checks whether a route can be claimed with the given amount of cards
     */
    fun canClaimWithCounts(
        route: Route, colorCardCount: Int, locomotiveCount: Int, otherCards: Int, exhaustive: Boolean
    ): ClaimRouteFailure? {
        var otherCardCount = otherCards
        when {
            route is Ferry -> {
                val requiredCount = if (colorCardCount >= route.length) {
                    otherCardCount += colorCardCount - route.length
                    0
                } else {
                    route.length - colorCardCount
                }
                val required = requiredCount + (route.ferries - locomotiveCount) - (otherCardCount / 3)
                if (exhaustive) {
                    if (required < 0 || (otherCardCount % 3 > 0)) return ClaimRouteFailure.TooManyCards
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    return null
                } else {
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    return null
                }
            }

            route is Tunnel -> {
                val required = route.length - locomotiveCount - colorCardCount
                if (exhaustive && otherCardCount != 0) return ClaimRouteFailure.IllegalCards
                if (exhaustive) {
                    if (required < 0) return ClaimRouteFailure.TooManyCards
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    return null
                } else {
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    return null
                }
            }

            route.isMurmanskLieksa() -> {
                val mixedBudget = otherCardCount + locomotiveCount
                val required = route.length - colorCardCount - (mixedBudget / 4)
                if (exhaustive) {
                    if (required < 0 || (otherCardCount % 4 > 0)) return ClaimRouteFailure.TooManyCards
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    return null
                } else {
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    return null
                }
            }

            else -> {
                val required = route.length - colorCardCount

                if (exhaustive) {
                    if (otherCardCount != 0 || locomotiveCount != 0) return ClaimRouteFailure.IllegalCards
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    if (required < 0) return ClaimRouteFailure.TooManyCards
                    return null
                } else {
                    if (required > 0) return ClaimRouteFailure.NotEnoughCards
                    return null
                }
            }
        }
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
        val betweenState = root.game.run { states[currentStateIndex] }
        val previousState = root.game.run { states[currentStateIndex - 1] }
        val prevPlayerHand = previousState.players[betweenState.currentPlayerIndex].wagonCards
        val betweenPlayerHand = betweenState.currentPlayer.wagonCards
        val handDiff = prevPlayerHand.count { oldCard -> betweenPlayerHand.none { newCard -> oldCard === newCard } }
        val (newDiscard, usedCards) = betweenState.discardStack.run { splitAt(size - handDiff) }
        val (newDraw, requiredCards) = betweenState.wagonCardsStack.run { splitAt(max(0, size - 3)) }

        if (cards == null) {
            root.undo()
            root.insert(
                previousState.copy(
                    discardStack = newDiscard + requiredCards,
                    wagonCardsStack = newDraw
                )
            )
            if (root.game.currentState.players.any { it.isRemote } && state.currentPlayer.name == root.network.client?.playerName) {
                val shuffled = previousState.discardStack.isNotEmpty() && newDiscard.isEmpty()
                val newCardStack = if (shuffled) state.wagonCardsStack else null
                root.network.sendClaimARounteMessage(route, newCardStack, usedCards, null)
            }
            root.game.gameState = GameState.DEFAULT
            root.gameService.nextPlayer()
            return
        }
        val (required, allowedColor) = tunnelPayAmount(usedCards)
        val given = cards.count { it.color == Color.JOKER || it.color == allowedColor }
        check(required == given && given == cards.size)
        val newPlayerHand = betweenState.currentPlayer.wagonCards.filter { card -> cards.none { it === card } }
        val newPlayers = betweenState.updateCurrentPlayer {
            copy(
                points = points + route.pointValue(),
                trainCarsAmount = trainCarsAmount - route.completeLength,
                claimedRoutes = claimedRoutes + route,
                wagonCards = newPlayerHand
            )
        }
        root.undo()
        root.insert(
            betweenState.copy(
                discardStack = betweenState.discardStack + requiredCards,
                wagonCardsStack = newDraw,
                players = newPlayers
            )
        )
        root.game.gameState = GameState.DEFAULT
        onAllRefreshables { refreshAfterAfterClaimTunnel(route) }
        if (root.game.currentState.players.any { it.isRemote } && state.currentPlayer.name == root.network.client?.playerName) {
            val shuffled = previousState.discardStack.isNotEmpty() && newDiscard.isEmpty()
            val newCardStack = if (shuffled) state.wagonCardsStack else null
            root.network.sendClaimARounteMessage(route, newCardStack, usedCards, cards)
        }
        root.gameService.nextPlayer()
    }

    fun tunnelPayAmount(usedCards: List<WagonCard>): Pair<Int, Color?> {
        val required = state.wagonCardsStack.run { subList(max(0, size - 3), size) }
        val colorCard = usedCards.firstOrNull { it.color != Color.JOKER }
        if (colorCard == null) {
            return required.count { it.color == Color.JOKER } to null
        } else {
            return required.count { it.color == colorCard.color || it.color == Color.JOKER } to colorCard.color
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


