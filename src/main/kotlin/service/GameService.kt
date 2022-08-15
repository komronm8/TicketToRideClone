package service

import entity.*
import java.util.IdentityHashMap

class GameService(val root:  RootService) {
    private var state: State
        get() = root.game.currentState
        set(value) {
            root.insert(value)
        }

    private fun updateWithFinalScore() {
        val scores = state.players.map(this::calcDestinationScore)
        val maxFulfilled = scores.maxOf { it.second }
        val newPlayers = state.players.mapIndexed { index, player ->
            val isGlobeTrotter = scores[index].second == maxFulfilled
            val additional = scores[index].first + if (isGlobeTrotter) 10 else 0
            player.copy(points = player.points + additional)
        }
        state = state.copy(players = newPlayers)
    }

    private fun calcDestinationScore(player: Player): Pair<Int, Int> {
        val cities = state.cities
        val claimedRoutes = IdentityHashMap<Route, Unit>(player.claimedRoutes.size)
        player.claimedRoutes.forEach { claimedRoutes.put(it, Unit)}
        val connectivity = IdentityHashMap<City, Int>()
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
                    if (!connectivity.contains(other)) {
                        connectivity[other] = groupId
                        calcConnectivity(other, groupId)
                    }
                }
            }
        }
        var groupIds = 0
        for (city in cities) {
            if (!connectivity.containsKey(city)) {
                calcConnectivity(city, groupIds++)
            }
        }
        var fulfilledCards = 0
        var scoreSum = 0
        for (card in player.destinationCards) {
            val fulfilled = connectivity[card.cities.first] == connectivity[card.cities.second]
            if (fulfilled) fulfilledCards += 1
            scoreSum += if (fulfilled) card.points else -card.points
        }
        return scoreSum to fulfilledCards
    }

    fun nextPlayer() {
        val oldState = state
        root.undo()
        //...
        val newState = oldState.copy(
            currentPlayerIndex = oldState.run { (currentPlayerIndex + 1) % players.size }
        )
        root.insert(newState)
    }
}