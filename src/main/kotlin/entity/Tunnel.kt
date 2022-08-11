package entity

/**
 * A tunnel route
 */
class Tunnel(
    length: Int,
    color: List<Color>,
    cities: Pair<City, City>,
) : Route(length, color, cities) {
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

    override fun toString(): String {
        return "Tunnel(${toStringCore()})"
    }
}