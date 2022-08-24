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
open class Player(
    val points: Int = 0,
    val name: String,
    val destinationCards: List<DestinationCard>,
    val wagonCards: List<WagonCard>,
    val trainCarsAmount: Int = 40,
    val claimedRoutes: List<Route> = emptyList(),
    val isRemote: Boolean
) {
    /**
     * Checks if two Players are equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (points != other.points) return false
        if (name != other.name) return false
        if (destinationCards != other.destinationCards) return false
        if (wagonCards != other.wagonCards) return false
        if (trainCarsAmount != other.trainCarsAmount) return false
        if (claimedRoutes != other.claimedRoutes) return false

        return true
    }

    /**
     * Generates Hashcode for [Player]
     */
    override fun hashCode(): Int {
        var result = points
        result = 31 * result + name.hashCode()
        result = 31 * result + destinationCards.hashCode()
        result = 31 * result + wagonCards.hashCode()
        result = 31 * result + trainCarsAmount
        result = 31 * result + claimedRoutes.hashCode()
        return result
    }

    /**
     * Copies this Player object
     */
    open fun copy(
        points: Int = this.points,
        destinationCards: List<DestinationCard> = this.destinationCards,
        wagonCards: List<WagonCard> = this.wagonCards,
        trainCarsAmount: Int = this.trainCarsAmount,
        claimedRoutes: List<Route> = this.claimedRoutes
    ): Player = Player(points, name, destinationCards, wagonCards, trainCarsAmount, claimedRoutes, isRemote)

    /**
     * Generates an offline Player
     */
    fun withNotRemote(): Player {
        return if (isRemote) {
            Player(points, name, destinationCards, wagonCards, trainCarsAmount, claimedRoutes, false)
        } else {
            this
        }
    }

    /**
     * Concerts [Player] data to string
     */
    override fun toString(): String {
        return "Player(points=$points, name='$name', destinationCards=$destinationCards," +
                " wagonCards=$wagonCards, trainCarsAmount=$trainCarsAmount, claimedRoutes=$claimedRoutes)"
    }


}