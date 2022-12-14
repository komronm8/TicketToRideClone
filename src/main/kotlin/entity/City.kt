package entity

/**
 * A City represented by its name and its connections to other cities
 *
 * @param name The name of the city
 * @param routes The connections to other cities
 */
class City(val name: String, val routes: List<Route>) {
    override fun toString(): String = "City(name = $name)"
}