package entity

/**
 * A class representing an AI player
 */
class AIPlayer(
    points: Int,
    name: String,
    destinationCards: List<DestinationCard>,
    wagonCards: List<WagonCard>,
    trainCarsAmount: Int,
    claimedRoutes: List<Route>,
    isRemote: Boolean,
): Player(points, name, destinationCards, wagonCards, trainCarsAmount, claimedRoutes, isRemote, )