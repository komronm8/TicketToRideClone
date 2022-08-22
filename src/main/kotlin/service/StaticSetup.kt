package service

import entity.*
import entity.Color.*

private fun MutableList<City>.city(name: String): Pair<City, MutableList<Route>>{
    val routes = mutableListOf<Route>()
    val city = City(name, routes)
    add(city)
    return city to routes
}
private fun Pair<Pair<City, MutableList<Route>>, Pair<City, MutableList<Route>>>.route(
    length: Int,
    color: Color,
    sibling: Route? = null
): Route {
    val route = Route(length, color, this.first.first to this.second.first, sibling)
    sibling?.also { it.sibling = route }
    this.first.second.add(route)
    this.second.second.add(route)
    return route
}

private fun Pair<Pair<City, MutableList<Route>>, Pair<City, MutableList<Route>>>.ferry(
    ferries: Int,
    length: Int,
    color: Color,
    sibling: Route? = null
): Ferry {
    assert(ferries <= length)
    val route = Ferry(ferries, length - ferries, color, this.first.first to this.second.first, sibling)
    sibling?.also { it.sibling = route }
    this.first.second.add(route)
    this.second.second.add(route)
    return route
}

private fun Pair<Pair<City, MutableList<Route>>, Pair<City, MutableList<Route>>>.tunnel(
    length: Int,
    color: Color,
    sibling: Route? = null
): Route {
    val route = Tunnel(length, color, this.first.first to this.second.first, sibling)
    sibling?.also { it.sibling = route }
    this.first.second.add(route)
    this.second.second.add(route)
    return route
}

private fun MutableList<City>.constructGraph() {
    val hon = city("Honningsvåg")
    val trom = city("Tromsø")
    val kirk = city("Kirkenes")
    val mur = city("Murmansk")
    val rov = city("Rovaniemi")
    val nar = city("Narvik")
    val kiru = city("Kiruna")
    val bod = city("Boden")
    val moi = city("Mo I Rana")
    val tor = city("Tornio")
    val oul = city("Oulu")
    val kaj = city("Kajaani")
    val lie = city("Lieksa")
    val kuo = city("Kuopio")
    val ima = city("Imatra")
    val hel = city("Helsinki")
    val lah = city("Lahti")
    val tam = city("Tampere")
    val tur = city("Turku")
    val tal = city("Tallinn")
    val sto = city("Stockholm")
    val nor = city("Norrköping")
    val kar = city("Karlskrona")
    val kob = city("København")
    val got = city("Göteborg")
    val aar = city("Århus")
    val aal = city("Ålborg")
    val ume = city("Umeå")
    val vaa = city("Vaasa")
    val sun = city("Sundsvall")
    val ore = city("Örebro")
    val osl = city("Oslo")
    val tron = city("Trondheim")
    val ost = city("Östersund")
    val and = city("Åndalsnes")
    val lil = city("Lillehammer")
    val ber = city("Bergen")
    val sta = city("Stavanger")
    val kri = city("Kristiansand")
    (hon to kirk).ferry(1, 2, GREEN)
    (kirk to mur).ferry(1, 2, WHITE)
    (kirk to rov).route(5,  BLUE)
    (trom to hon).ferry(2, 4, PURPLE)
    (nar to trom).ferry(1, 3, YELLOW)
    (moi to nar).ferry(2, 4, ORANGE)
    (tron to moi).tunnel(5, GREEN, (tron to moi).ferry(2, 6, RED))
    (and to tron).ferry(1, 2, WHITE)
    (ber to and).ferry(2, 5, BLACK)
    (sta to ber).ferry(1, 2, PURPLE)
    (kri to sta).tunnel(2, GREEN, (kri to sta).ferry(1, 3, ORANGE))
    (kri to osl).route(2,  BLACK)
    (osl to ber).tunnel(4, RED, (osl to ber).tunnel(4, BLUE))
    (osl to  lil).tunnel(2, PURPLE)
    (lil to and).tunnel(2, YELLOW)
    (lil to tron).tunnel(3, ORANGE)
    (aal to kri).ferry(1, 2 , RED)
    (aal to osl).ferry(1, 3, WHITE)
    (aar to aal).route(1, PURPLE)
    (kob to aar).ferry(1, 1, JOKER)
    (kob to kar).ferry(1, 2, GREEN, (kob to kar).ferry(1, 2, BLUE))
    (kob to got).ferry(1, 2, BLACK)
    (got to aal).ferry(1, 2, JOKER)
    (got to osl).route(2, ORANGE)
    (got to ore).route(2, BLUE)
    (got to nor).route(3, JOKER)
    (nor to kar).route(3, WHITE, (nor to kar).route(3, YELLOW))
    (nor to sto).route(1, ORANGE, (nor to sto).route(1, RED))
    (nor to ore).route(2, JOKER)
    (ore to osl).route(2, YELLOW, (ore to osl).route(2, GREEN))
    (ore to sto).route(2, PURPLE, (ore to sto).route(2, BLACK))
    (ore to sun).route(4, ORANGE)
    (sun to ost).route(2, GREEN)
    (ost to tron).tunnel(2, BLACK)
    (sun to ume).route(3, PURPLE, (sun to ume).route(3, YELLOW))
    (sun to sto).route(4, JOKER, (sun to sto).route(4,  JOKER))
    (ume to bod).route(3, WHITE, (ume to bod).route(3, RED))
    (bod to kiru).route(3, ORANGE, (bod to kiru).route(3, BLACK))
    (kiru to nar).tunnel(1, PURPLE, (kiru to nar).tunnel(1, WHITE))
    (bod to tor).route(1, GREEN)
    (tor to rov).route(1, RED)
    (tor to oul).route(1, WHITE)
    (oul to rov).route(2, ORANGE)
    (oul to kaj).route(2, YELLOW)
    (kaj to lie).route(1, BLUE)
    (lie to mur).route(9, JOKER)
    (lie to kuo).route(1, BLACK)
    (kuo to ima).route(2, PURPLE)
    (kuo to lah).route(3, WHITE)
    (kuo to kaj).route(2, GREEN)
    (kuo to oul).route(3, JOKER)
    (kuo to vaa).route(4,  JOKER)
    (vaa to oul).route(3, BLACK)
    (vaa to ume).ferry(1, 1, JOKER)
    (vaa to sun).ferry(1, 3, BLUE)
    (vaa to tam).route(2, PURPLE)
    (tam to tur).route(1, RED)
    (tam to hel).route(1,  ORANGE)
    (tam to lah).route(1, BLUE)
    (lah to ima).route(2, YELLOW)
    (lah to hel).route(1, BLACK)
    (hel to ima).route(3, RED)
    (hel to tal).ferry(1, 2, PURPLE)
    (tal to sto).ferry(2, 4, GREEN)
    (hel to sto).ferry(1, 4, YELLOW, (hel to sto).ferry(2, 4, JOKER))
    (hel to tur).route(1, WHITE)
    (tur to sto).ferry(1, 3, BLUE)
}

/**
 * Constructs a graph of cities all cities and routes in the game
 */
fun constructGraph(): List<City> =
    ArrayList<City>(38).also { it.constructGraph() }

/**
 * Creates all destination cards in the game from the given cities
 */
fun destinationPool(cities: Map<String, City>): List<DestinationCard> {
    val cards = mutableListOf<DestinationCard>()
    cards += DestinationCard(8, checkNotNull(cities["Bergen"]) to checkNotNull(cities["København"]))
    cards += DestinationCard(16, checkNotNull(cities["Bergen"]) to checkNotNull(cities["Narvik"]))
    cards += DestinationCard(17, checkNotNull(cities["Bergen"]) to checkNotNull(cities["Tornio"]))
    cards += DestinationCard(7, checkNotNull(cities["Bergen"]) to checkNotNull(cities["Trondheim"]))
    cards += DestinationCard(12, checkNotNull(cities["Göteborg"]) to checkNotNull(cities["Oulu"]))
    cards += DestinationCard(7, checkNotNull(cities["Göteborg"]) to checkNotNull(cities["Turku"]))
    cards += DestinationCard(6, checkNotNull(cities["Göteborg"]) to checkNotNull(cities["Åndalsnes"]))
    cards += DestinationCard(12, checkNotNull(cities["Helsinki"]) to checkNotNull(cities["Bergen"]))
    cards += DestinationCard(13, checkNotNull(cities["Helsinki"]) to checkNotNull(cities["Kirkenes"]))
    cards += DestinationCard(10, checkNotNull(cities["Helsinki"]) to checkNotNull(cities["Kiruna"]))
    cards += DestinationCard(10, checkNotNull(cities["Helsinki"]) to checkNotNull(cities["København"]))
    cards += DestinationCard(5, checkNotNull(cities["Helsinki"]) to checkNotNull(cities["Lieksa"]))
    cards += DestinationCard(8, checkNotNull(cities["Helsinki"]) to checkNotNull(cities["Östersund"]))
    cards += DestinationCard(24, checkNotNull(cities["København"]) to checkNotNull(cities["Murmansk"]))
    cards += DestinationCard(18, checkNotNull(cities["København"]) to checkNotNull(cities["Narvik"]))
    cards += DestinationCard(14, checkNotNull(cities["København"]) to checkNotNull(cities["Oulu"]))
    cards += DestinationCard(12, checkNotNull(cities["Kristiansand"]) to checkNotNull(cities["Mo I Rana"]))
    cards += DestinationCard(12, checkNotNull(cities["Narvik"]) to checkNotNull(cities["Murmansk"]))
    cards += DestinationCard(13, checkNotNull(cities["Narvik"]) to checkNotNull(cities["Tallinn"]))
    cards += DestinationCard(11, checkNotNull(cities["Norrköping"]) to checkNotNull(cities["Boden"]))
    cards += DestinationCard(8, checkNotNull(cities["Oslo"]) to checkNotNull(cities["Helsinki"]))
    cards += DestinationCard(4, checkNotNull(cities["Oslo"]) to checkNotNull(cities["København"]))
    cards += DestinationCard(21, checkNotNull(cities["Oslo"]) to checkNotNull(cities["Honningsvåg"]))
    cards += DestinationCard(10, checkNotNull(cities["Oslo"]) to checkNotNull(cities["Mo I Rana"]))
    cards += DestinationCard(4, checkNotNull(cities["Oslo"]) to checkNotNull(cities["Stavanger"]))
    cards += DestinationCard(4, checkNotNull(cities["Oslo"]) to checkNotNull(cities["Stockholm"]))
    cards += DestinationCard(9, checkNotNull(cities["Oslo"]) to checkNotNull(cities["Vaasa"]))
    cards += DestinationCard(8, checkNotNull(cities["Stavanger"]) to checkNotNull(cities["Karlskrona"]))
    cards += DestinationCard(18, checkNotNull(cities["Stavanger"]) to checkNotNull(cities["Rovaniemi"]))
    cards += DestinationCard(8, checkNotNull(cities["Stockholm"]) to checkNotNull(cities["Bergen"]))
    cards += DestinationCard(7, checkNotNull(cities["Stockholm"]) to checkNotNull(cities["Imatra"]))
    cards += DestinationCard(10, checkNotNull(cities["Stockholm"]) to checkNotNull(cities["Kajaani"]))
    cards += DestinationCard(6, checkNotNull(cities["Stockholm"]) to checkNotNull(cities["København"]))
    cards += DestinationCard(17, checkNotNull(cities["Stockholm"]) to checkNotNull(cities["Tromsø"]))
    cards += DestinationCard(7, checkNotNull(cities["Stockholm"]) to checkNotNull(cities["Umeå"]))
    cards += DestinationCard(6, checkNotNull(cities["Sundsvall"]) to checkNotNull(cities["Lahti"]))
    cards += DestinationCard(6, checkNotNull(cities["Tampere"]) to checkNotNull(cities["Boden"]))
    cards += DestinationCard(10, checkNotNull(cities["Tampere"]) to checkNotNull(cities["Kristiansand"]))
    cards += DestinationCard(3, checkNotNull(cities["Tampere"]) to checkNotNull(cities["Tallinn"]))
    cards += DestinationCard(6, checkNotNull(cities["Tornio"]) to checkNotNull(cities["Imatra"]))
    cards += DestinationCard(11, checkNotNull(cities["Tromsø"]) to checkNotNull(cities["Vaasa"]))
    cards += DestinationCard(10, checkNotNull(cities["Turku"]) to checkNotNull(cities["Trondheim"]))
    cards += DestinationCard(11, checkNotNull(cities["Ålborg"]) to checkNotNull(cities["Umeå"]))
    cards += DestinationCard(5, checkNotNull(cities["Ålborg"]) to checkNotNull(cities["Norrköping"]))
    cards += DestinationCard(6, checkNotNull(cities["Århus"]) to checkNotNull(cities["Lillehammer"]))
    cards += DestinationCard(10, checkNotNull(cities["Örebro"]) to checkNotNull(cities["Kuopio"]))
    return cards
}