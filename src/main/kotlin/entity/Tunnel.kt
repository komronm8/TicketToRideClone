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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tunnel) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        //Additional multiplication to generate unique hashcode
        return super.hashCode() * 31 + 13
    }

    override fun reducedToString(): String = "Tunnel(length = $length, color = $length)"

    override fun toString(): String {
        return "Tunnel(${toStringCore()})"
    }
}