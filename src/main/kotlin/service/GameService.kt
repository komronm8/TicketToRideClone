package service

import entity.*
import service.message.DestinationTicket
import view.Refreshable
import java.util.*

/**
 * A service responsible for performing the system actions
 */
class GameService(val root: RootService) : AbstractRefreshingService() {
    /**
     * The player data necessary to construct an initial [Player]
     * @param aiStrategy determines whether this player is an AI player.
     */
    data class PlayerData(val name: String, val isRemote: Boolean, val aiStrategy: AIPlayer.Strategy? = null)

    private var state: State
        get() = root.game.currentState
        set(value) {
            root.insert(value)
        }

    var chosenCards: MutableMap<String, List<Int>> = mutableMapOf()

    /**
     * Starts a game with the given player data. At the end, the game is in a valid state, but the player still needs
     * to select which cards can be kept and which discarded, to perform that decision call [chooseDestinationCard]
     */
    fun startNewGame(playerNames: List<PlayerData>) {
        fun <T> MutableList<T>.popAll(count: Int): List<T> {
            val retain = subList(size - count, size).toList()
            repeat(count) { removeLast() }
            return retain
        }
        check(playerNames.map(PlayerData::name).distinct().size == playerNames.size)
        val cities = constructGraph()
        val destinations = destinationPool(cities.associateBy { it.name }).shuffled().toMutableList()
        val wagonCards = (Color.values().flatMap { color -> List(12) { WagonCard(color) } } +
                List(2) { WagonCard(Color.JOKER) }).shuffled().toMutableList()
        val players = playerNames.map {
            if (it.aiStrategy != null) {
                AIPlayer(
                    name = it.name,
                    destinationCards = destinations.popAll(5),
                    wagonCards = wagonCards.popAll(4),
                    strategy = it.aiStrategy
                )
            } else {
                Player(
                    name = it.name,
                    destinationCards = destinations.popAll(5),
                    wagonCards = wagonCards.popAll(4),
                    isRemote = it.isRemote
                )
            }
        }
        root.game = Game(State(
            cities = cities,
            openCards = wagonCards.popAll(5),
            wagonCardsStack = wagonCards,
            players = players,
            destinationCards = destinations,
        ))

        root.game.gameState = GameState.CHOOSE_DESTINATION_CARD
        onAllRefreshables(Refreshable::refreshAfterStartNewGame)
        if (root.game.currentState.players.any { it.isRemote }){
            root.network.startNewHostedGame(state)
        }
    }

    /**
     * Is called after all players decided which of their cards to keep
     */
    fun chooseDestinationCard(cards: Map<String, List<Int>>) {
        if (root.game.gameState != GameState.CHOOSE_DESTINATION_CARD) {
            throw IllegalStateException("game is not in the right state for choose destination card")
        }
        require(cards.size == state.players.size)
        cards.forEach {
            assert(it.value.size in 2..5)
            assert(it.value.distinct().size == it.value.size)
            it.value.forEach { index -> assert(index in 0 until 5) }
        }
        val newPlayers = state.players.map {
            val indices=  requireNotNull(cards[it.name])
            it.copy(destinationCards = indices.map(it.destinationCards::get))
        }
        root.game.gameState = GameState.DEFAULT
        root.game.states[0] = state.copy(players = newPlayers)
        onAllRefreshables(Refreshable::refreshAfterChooseDestinationCard)
    }

    fun chooseDestinationCards(playerName: String, cards: List<Int>){
        chosenCards[playerName] = cards

        if (chosenCards.size >= state.players.size){
            chooseDestinationCard(chosenCards)
            chosenCards.clear()
        }

        onAllRefreshables(Refreshable::refreshAfterOneDestinationCard)

        if (root.game.currentState.players.any { it.isRemote }){
            root.network.GameInitResponseMessage(cards.map { index -> state.players.first { it.name == playerName }.destinationCards[index] })
        }
    }

    /**
     * Called at the end of the game. Calculates the final scores and the winner.
     * Calls [Refreshable.refreshAfterEndGame] with the winner.
     */
    fun endGame() {
        val winner = updateWithFinalScore()

        onAllRefreshables { refreshAfterEndGame(winner) }
    }

    fun nextGame() {
        startNewGame(state.players.map { PlayerData(it.name, it.isRemote) })
    }

    private fun updateWithFinalScore(): Player {
        val scores = state.players.map(this::calcDestinationScore)
        val maxFulfilled = scores.maxOf { it.second }
        val newPlayers = state.players.mapIndexed { index, player ->
            val isGlobeTrotter = scores[index].second == maxFulfilled
            val additional = scores[index].first + if (isGlobeTrotter) 10 else 0
            player.copy(points = player.points + additional)
        }
        state = state.copy(players = newPlayers)
        val sorted = state.players.sortedByDescending { it.points }
        val winner = if (sorted[0].points != sorted[1].points)
            sorted[0]
        else {
            val mostFulfilled = newPlayers.filterIndexed { index, _ -> scores[index].second == maxFulfilled }
            if (mostFulfilled.size == 1) {
                mostFulfilled[0]
            } else {
                checkNotNull(newPlayers.maxByOrNull { it.claimedRoutes.maxOfOrNull { it.completeLength } ?: 0 })
            }
        }
        return winner
    }

    private fun calcDestinationScore(player: Player): Pair<Int, Int> {
        val cities = state.cities
        val claimedRoutes = IdentityHashMap<Route, Unit>(player.claimedRoutes.size)
        player.claimedRoutes.forEach { claimedRoutes.put(it, Unit) }
        val groups = IdentityHashMap<City, Int>()
        fun calcConnectivity(
            city: City,
            groupId: Int,
        ) {
            for (neighbor in city.routes) {
                if (claimedRoutes.contains(neighbor)) {
                    val other = if (neighbor.cities.first === city) {
                        neighbor.cities.second
                    } else {
                        neighbor.cities.first
                    }
                    if (!groups.contains(other)) {
                        groups[other] = groupId
                        calcConnectivity(other, groupId)
                    }
                }
            }
        }

        var groupIds = 0
        for (city in cities) {
            if (!groups.containsKey(city)) {
                val groupId = groupIds++
                groups[city] = groupId
                calcConnectivity(city, groupId)
            }
        }
        var fulfilledCards = 0
        var scoreSum = 0
        for (card in player.destinationCards) {
            val fulfilled = groups[card.cities.first] == groups[card.cities.second]
            if (fulfilled) fulfilledCards += 1
            scoreSum += if (fulfilled) card.points else -card.points
        }
        return scoreSum to fulfilledCards
    }

    /**
     * Advances to the next player, calls [endGame] if the current player was
     * the last player.
     */
    fun nextPlayer() {
        if (state.currentPlayer.name == state.endPlayer?.name) {
            endGame()
        }
        val oldState = state
        root.undo()
        val endPlayer = if (oldState.currentPlayer.trainCarsAmount <= 2) {
            oldState.currentPlayer
        } else {
            null
        }
        val newState = oldState.copy(
            currentPlayerIndex = oldState.run { (currentPlayerIndex + 1) % players.size },
            endPlayer = oldState.endPlayer ?: endPlayer
        )
        root.insert(newState)
        onAllRefreshables(Refreshable::refreshAfterNextPlayer)
    }

}