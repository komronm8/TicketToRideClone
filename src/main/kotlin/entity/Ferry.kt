package entity

/**
 * This class represents a ferry route.
 * @param ferries The amount of locomotive cards required to claim this route.
 * The requirements specified in [Route] still apply
 */
class Ferry(
    val ferries: Int,
    length: Int,
    color: Color,
    cities: Pair<City, City>,
    sibling: Route? = null,
): Route(length, color, cities, sibling) {
    override val completeLength: Int
        get() = super.completeLength + ferries
    override fun toString(): String {
        return "Ferry(${toStringCore()}, ferries: $ferries)"
    }

    override fun reducedToString(): String = "Ferry(length = $length, color = $length, ferries = $ferries)"
}