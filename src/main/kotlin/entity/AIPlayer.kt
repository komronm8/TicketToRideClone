package entity

/**
 * A class representing an AI player
 */
class AIPlayer(
    name: String,
    destinationCards: List<DestinationCard>,
    wagonCards: List<WagonCard>,
    points: Int = 0,
    trainCarsAmount: Int = 40,
    claimedRoutes: List<Route> = emptyList(),
    val strategy: Strategy
): Player(points, name, destinationCards, wagonCards, trainCarsAmount, claimedRoutes, false) {
    enum class Strategy {
        RANDOM, MONTE_CARLO,
    }
    override fun copy(
        points: Int,
        destinationCards: List<DestinationCard>,
        wagonCards: List<WagonCard>,
        trainCarsAmount: Int,
        claimedRoutes: List<Route>
    ): Player {
        return AIPlayer(this.name, destinationCards, wagonCards, trainCarsAmount, points, claimedRoutes, this.strategy)
    }
}