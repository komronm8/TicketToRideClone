package entity

/**
 * This class represents a ferry route.
 * @param ferries The amount of locomotive cards required to claim this route.
 * The requirements specified in [Route] still apply
 */
class Ferry(
    val ferries: Int,
    length: Int,
    color: List<Color>,
    cities: Pair<City, City>,
): Route(length, color, cities) {
    override val completeLength: Int
        get() = super.completeLength + ferries

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ferry) return false
        if (!super.equals(other)) return false

        if (ferries != other.ferries) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + ferries
        return result
    }
    override fun toString(): String {
        return "Ferry(${toStringCore()}, ferries: $ferries)"
    }
}