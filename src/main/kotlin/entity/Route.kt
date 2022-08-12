package entity

/**
 * A Route connecting two cities
 *
 * @param length The length of the route, meaning the amount of [WagonCards][WagonCard] required to claim it
 * @param color The color of the route
 * @param cities The two connected cities
 */
open class Route(val length: Int, val color: Color, val cities: Pair<City, City>, var sibling: Route? = null) {
    /**
     * The complete length of the route(may differ when route is [Ferry])
     */
    open val completeLength: Int
        get() = length

    /**
     * Checks whether this route is the route between Murmansk and Lieksa
     */
    fun isMurmanskLieksa() = (cities.first.name == "Murmansk" && cities.second.name == "Lieksa") ||
            (cities.second.name == "Murmansk" && cities.first.name == "Lieksa")
    protected open fun reducedToString() = "Route(length = $length, color = $color)"
    protected fun toStringCore(): String {
        val siblingText = sibling?.reducedToString() ?: "()"
        return "${cities.first} to ${cities.second}, length=$length, color=$color, sibling: $siblingText"
    }
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

        if (sibling != other.sibling) return false

        return true
    }

    override fun hashCode(): Int {
        var result = length
        result = 31 * result + color.hashCode()
        result = 31 * result + cities.hashCode()
        result = 31 * result + (sibling?.hashCode() ?: 0)
        return result
    }

}