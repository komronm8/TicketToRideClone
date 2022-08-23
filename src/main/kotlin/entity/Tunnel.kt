package entity

/**
 * A tunnel route
 */
class Tunnel(
    length: Int,
    color: Color,
    cities: Pair<City, City>,
    id: Int,
    sibling: Route? = null,
) : Route(length, color, cities, id, sibling) {
    override fun reducedToString(): String = "Tunnel(length = $length, color = $length)"

    override fun toString(): String {
        return "Tunnel(${toStringCore()})"
    }
}