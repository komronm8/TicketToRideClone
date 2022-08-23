package entity

/**
 * A Route connecting two cities
 *
 * @param length The length of the route, meaning the amount of [WagonCards][WagonCard] required to claim it
 * @param color The color of the route
 * @param cities The two connected cities
 */
open class Route(val length: Int, val color: Color, val cities: Pair<City, City>, val id: Int, var sibling: Route? = null) {
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
        return "${cities.first} to ${cities.second}, length=$length, color=$color, sibling: $siblingText, id: $id"
    }
    override fun toString(): String {
        return "Route(${toStringCore()})"
    }

}