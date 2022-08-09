package entity

open class Route(val length: Int, val color: Color, val cities: Pair<City, City>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        if (length != other.length) return false
        if (color != other.color) return false
        if (cities != other.cities) return false

        return true
    }

    override fun hashCode(): Int {
        var result = length
        result = 31 * result + color.hashCode()
        result = 31 * result + cities.hashCode()
        return result
    }

    protected fun toStringCore(): String =
        "${cities.first} to ${cities.second}, length=$length, color=$color"
    override fun toString(): String {
        return "Route(${toStringCore()})"
    }
}