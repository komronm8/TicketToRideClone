package entity

/**
 * A Route connecting two cities
 *
 * @param length The length of the route, meaning the amount of [WagonCards][WagonCard] required to claim it
 * @param color The color of the route
 * @param cities The two connected cities
 * @param claimedBy The player who claimed this route
 */
open class Route(val length: Int, val color: Color, val cities: Pair<City, City>, val claimedBy: Player? = null) {
    /**
     * The complete length of the route(may differ when route is [Ferry])
     */
    open val completeLength: Int
        get() = length

    /**
     * Copies this route, but with the [claimedBy] set to [player]
     *
     * @param player The player wo which [claimedBy] is set
     */
    open fun asClaimedBy(player: Player): Route = Route(length, color, cities, player)

    protected fun toStringCore(): String =
        "${cities.first} to ${cities.second}, length=$length, color=$color, claimed by=$claimedBy"
    override fun toString(): String {
        return "Route(${toStringCore()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        if (length != other.length) return false
        if (color != other.color) return false
        if (cities != other.cities) return false
        if (claimedBy != other.claimedBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = length
        result = 31 * result + color.hashCode()
        result = 31 * result + cities.hashCode()
        result = 31 * result + (claimedBy?.hashCode() ?: 0)
        return result
    }
}