package entity

import kotlin.math.sqrt

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
) : Player(points, name, destinationCards, wagonCards, trainCarsAmount, claimedRoutes, false) {
    /**
     * The AI Strategy to used
     */
    sealed interface Strategy {
        /**
         * The random AI
         */
        object Random : Strategy

        /**
         * The monte-carlo-search algorithm
         * @param timeLimit The time frame in which the algorithm has to find the best move
         */
        data class MonteCarlo(val timeLimit: Int, val c: Double = sqrt(2.0)): Strategy
    }

    override fun copy(
        points: Int,
        destinationCards: List<DestinationCard>,
        wagonCards: List<WagonCard>,
        trainCarsAmount: Int,
        claimedRoutes: List<Route>
    ): Player {
        return AIPlayer(this.name, destinationCards, wagonCards, points, trainCarsAmount, claimedRoutes, this.strategy)
    }

    override fun toString(): String {
        return "AI"
    }
}