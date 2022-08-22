package entity

/**
 * A tunnel route
 */
class Tunnel(
    length: Int,
    color: Color,
    cities: Pair<City, City>,
    sibling: Route? = null,
) : Route(length, color, cities, sibling) {
    override fun reducedToString(): String = "Tunnel(length = $length, color = $length)"

    override fun toString(): String {
        return "Tunnel(${toStringCore()})"
    }
}