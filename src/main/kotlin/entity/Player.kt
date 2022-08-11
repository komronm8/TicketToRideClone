package entity

/**
 * The Player in the game
 *
 * @param points The points this player has
 * @param name The name of the player
 * @param destinationCards The [DestinationCard]s this player must fulfill
 * @param wagonCards The wagon cards this player has available
 * @param trainCarsAmount The amount of train cars, which are used to mark a route as claimed, left
 */
data class Player(
    val points: Int,
    val name: String,
    val destinationCards: List<DestinationCard>,
    val wagonCards: List<WagonCard>,
    val trainCarsAmount: Int,
    val claimedRoutes: List<Pair<Route, Color>>
)