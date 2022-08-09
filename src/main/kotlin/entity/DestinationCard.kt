package entity

/**
 * A Bounty for connecting the two [cities]
 *
 * @param points The worth of the bounty. The player will be rewarded the amount once they complete the conection
 * between the two  cities
 * @param cities The two  cities which have to be connected
 */
data class DestinationCard (val points: Int, val cities: Pair<City, City>)