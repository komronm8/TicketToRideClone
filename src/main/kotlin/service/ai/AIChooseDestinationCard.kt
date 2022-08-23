package service.ai

import entity.City
import entity.DestinationCard
import service.*

/**
 * Berechnet, gegeben 5 Destinationcards, eine Teilmenge (Größe 2/3) mit maximaler Überdeckung
 * Bei mehreren maximalen Überdeckung wählt er die Teilmenge mit der niedrigsten Durchschnittslänge
 */
public class AIChooseDestinationCard {

    /**
     * Alle Städte in Reihenfolge zur Indizierung
     */
    private val citiesInOrder = arrayOf("Honningsvåg",
        "Tromsø",
        "Kirkenes",
        "Murmansk",
        "Rovaniemi",
        "Narvik",
        "Kiruna",
        "Boden",
        "Mo I Rana",
        "Tornio",
        "Oulu",
        "Kajaani",
        "Lieksa",
        "Kuopio",
        "Imatra",
        "Helsinki",
        "Lahti",
        "Tampere",
        "Turku",
        "Tallinn",
        "Stockholm",
        "Norrköping",
        "Karlskrona",
        "København",
        "Göteborg",
        "Århus",
        "Ålborg",
        "Umeå",
        "Vaasa",
        "Sundsvall",
        "Örebro",
        "Oslo",
        "Trondheim",
        "Östersund",
        "Åndalsnes",
        "Lillehammer",
        "Bergen",
        "Stavanger",
        "Kristiansand")

    /**
     * Stadt zu Index
     */
    private fun cityToIndex(city: City?): Int {
        return citiesInOrder.indexOf(city?.name);
    }

    /**
     * Index zu Stadt
     */
    private fun indexToCity(index: Int): City? {
        val cityName = citiesInOrder[index];
        return cities.get(cityName);
    }

    /**
     * Stadtnamen an Städte gemappt
     */
    private val cities = constructGraph().associateBy { it.name }

    /**
     * Wählt eine Teilmenge von DestinationCards
     */
    fun chooseDestinationCards(destCards: ArrayList<DestinationCard>): ArrayList<DestinationCard> {
        val allComb = allCombinations(destCards);

        var candidate = ArrayList<ArrayList<DestinationCard>>();

        var maxUeberdeckung = 0;
        for(i in 0 until allComb.size) {
            if(ueberdeckung(allComb[i]) > maxUeberdeckung) {
                maxUeberdeckung = ueberdeckung(allComb[i])
                candidate.clear()
                candidate.add(allComb[i])
            }
            else if(ueberdeckung(allComb[i]) == maxUeberdeckung) {
                candidate.add(allComb[i])
            }
        }

        var finalChoice = ArrayList<DestinationCard>();

        if(candidate.size > 1) {
            var minAvg = 1000f
            for(i in 0 until candidate.size) {
                if(avaragePoints(candidate[i]) < minAvg) {
                    minAvg = avaragePoints(candidate[i])
                    finalChoice = candidate[i]
                }
            }
        }
        if(candidate.size == 1) {
            finalChoice = candidate[0]
        }

        return finalChoice
    }

    /**
     * Durchschnittspunktzahl einer Menge von Destinationcards
     */
    private fun avaragePoints(destCards: ArrayList<DestinationCard>): Float {
        var sum = 0f;
        for(i in 0 until destCards.size) {
            sum += destCards[i].points
        }
        return (sum/destCards.size)
    }

    /**
     * Wie viele Routen teilt eine Menge von DestinationCards?
     */
    private fun ueberdeckung(destCards: ArrayList<DestinationCard>): Int {

        var commonRoutes = 0;

        val firstPath = destCardToPath(destCards[0])
        val secondPath = destCardToPath(destCards[1])

        if(destCards.size == 2) {
            for(i in 0 until firstPath.size) {
                if(secondPath.contains(firstPath[i]) || secondPath.contains(swapped(firstPath[i]))) {
                    commonRoutes += 1
                }
            }
        }

        if(destCards.size == 3) {
            val thirdPath = destCardToPath(destCards[2])

            var commonPaths = ArrayList<Pair<City, City>>();

            for(i in 0 until firstPath.size) {
                if(secondPath.contains(firstPath[i]) || secondPath.contains(swapped(firstPath[i]))) {
                    commonRoutes += 1
                    commonPaths.add(firstPath[i])
                }
            }

            for(i in 0 until thirdPath.size) {
                if(commonPaths.contains(thirdPath[i])) {
                    commonRoutes += 2
                }
                else if(!commonPaths.contains(thirdPath[i])
                    && (firstPath.contains(thirdPath[i]) || firstPath.contains((swapped(thirdPath[i]))))
                    || (secondPath.contains(thirdPath[i]) || firstPath.contains((swapped(thirdPath[i]))))) {
                    commonRoutes += 1
                }
            }
        }

        return commonRoutes
    }

    /**
     * Ein Paar getauscht
     */
    private fun swapped(pair: Pair<City, City>): Pair<City, City> {
        return Pair(pair.second, pair.first)
    }

    /**
     * Alle Kombinationen (Größe 2/3) einer Menge von DestinationCards
     */
    private fun allCombinations(destCards: ArrayList<DestinationCard>): ArrayList<ArrayList<DestinationCard>> {
        // Berechnet alle 2er Kombinationen aus 5 gegebenenen Karten
        var globalList = ArrayList<ArrayList<DestinationCard>>()
        for(i in 0 until 5) {
            val cur = destCards[i];
            for(j in 0 until 5) {
                if(i != j) {
                    var list = ArrayList<DestinationCard>()
                    list.add(cur)
                    list.add(destCards[j])

                    if(!combContains(globalList, list)) {
                        globalList.add(list)
                    }
                }
            }
        }
        // Berechnet alle 3er Kombinationen aus 5 gegebenenen Karten
        for(i in 0 until globalList.size) {
            for(j in 0 until destCards.size) {
                if(!globalList[i].contains(destCards[j])) {
                    val newList = (globalList[i] + destCards[j]) as ArrayList<DestinationCard>
                    if(!combContains(globalList, newList)) {
                        globalList.add(newList)
                    }
                }
            }
        }
        return globalList
    }

    /**
     * Überprüft ob Menge a einer Menge b oder anderen Konstellation von b entspricht
     */
    private fun combEquals(a: ArrayList<DestinationCard>, b: ArrayList<DestinationCard>): Boolean {
        if(a.size != b.size) {
            return false;
        }
        for(i in 0 until a.size) {
            if(!b.contains(a[i])) {
                return false
            }
        }
        return true
    }

    /**
     * Enthält eine Liste eine Sub-Liste
     */
    private fun combContains(topList: ArrayList<ArrayList<DestinationCard>>, subList: ArrayList<DestinationCard>): Boolean {
        for(i in 0 until topList.size) {
            if(combEquals(topList[i],subList)) {
                return true;
            }
        }
        return false
    }

    /**
     * Erställt Pfad vom Startknoten aus
     * @param destCard Startpunkt
     */
    private fun destCardToPath(destCard: DestinationCard): ArrayDeque<Pair<City, City>> {
        val vorgaenger = Dijsktra(destCard.cities.first);
        var destinationCardPath = ArrayDeque<Pair<City, City>>();

        var currentCityIndex = cityToIndex(destCard.cities.second)

        while(vorgaenger[currentCityIndex] != null) {
            val pair = Pair(vorgaenger[currentCityIndex], indexToCity(currentCityIndex))
            destinationCardPath.addFirst(pair as Pair<City, City>)
            currentCityIndex = cityToIndex(vorgaenger[currentCityIndex])
        }
        return destinationCardPath;
    }

    /**
     * Berechnet mit Hilfe vom Dijkstra-Algorithmus ein Vorgängerarray,
     * mit dem alle kürzesten Pfade berechnet werden können
     */
    private fun Dijsktra(start: City): Array<City?> {

        // Dijkstra Init ////////////////////////////////////////////
        var abstand = IntArray(39) { Int.MAX_VALUE }
        var vorgaenger = arrayOfNulls<City>(39)
        var bekannt = BooleanArray(39) { false }


        var queue = ArrayList<City>()

        val startCityIndex = cityToIndex(start)

        abstand[startCityIndex] = 0
        queue.addAll(cities.values);
        //////////////////////////////////////////////////////////////
        var smallestElementIndex = startCityIndex;

        // Dijsktra
        while(queue.isNotEmpty()) {
            var curMin = Int.MAX_VALUE
            for(i in 0 until abstand.size) {
                if(!bekannt[i] && abstand[i] < curMin) {
                    smallestElementIndex = i;
                    curMin = abstand[i]
                }
            }

            bekannt[smallestElementIndex] = true

            val u = checkNotNull(indexToCity(smallestElementIndex))

            queue.remove(u)

            val routes = u.routes
            val neighbors = ArrayList<City>()

            for(i in 0 until routes.size) {
                val curRoute = routes.get(i)
                if(curRoute.cities.first != u) {
                    if(!neighbors.contains(curRoute.cities.first)) {
                        neighbors.add(curRoute.cities.first)
                    }
                }
                else {
                    if(!neighbors.contains(curRoute.cities.second)) {
                        neighbors.add(curRoute.cities.second)
                    }
                }
            }

            for(i in 0 until neighbors.size) {
                val v = neighbors.get(i)

                if(queue.contains(v)) {
                    distanceUpdate(u,v,abstand,vorgaenger)
                }
            }
        }

        return vorgaenger;
    }

    /**
     * Dijkstra-Hilfsmethode zum überschreiben bei Fund eines besseren Pfades
     */
    private fun distanceUpdate(u: City, v: City, abstand: IntArray, vorgaenger: Array<City?>) {
        var distBetween = 0

        for(i in 0 until u.routes.size) {
            if(u.routes.get(i).cities.first == v || u.routes.get(i).cities.second == v) {
                distBetween = u.routes.get(i).completeLength
            }
        }

        val alt = abstand[cityToIndex(u)] + distBetween

        if(alt < abstand[cityToIndex(v)]) {
            abstand[cityToIndex(v)] = alt
            vorgaenger[cityToIndex(v)] = u
        }
    }
}