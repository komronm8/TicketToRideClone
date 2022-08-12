package entity

/**
 * The game state at a given point in time.
 *
 * @param destinationCards The destination card draw stack
 * @param openCards The openly visible drawable wagon cards
 * @param wagonCardsStack The hidden wagon card draw stack
 * @param discardStack The discard stack for wagon cards
 * @param players The players
 * @param currentPlayerIndex The index of the current player
 * @param endPlayer The first player whose [Player.trainCarsAmount] is less than two
 * @param cities The cities involved in the game
 */
data class State(
    val destinationCards: List<DestinationCard>,

    val openCards: List<WagonCard>,
    val wagonCardsStack: List<WagonCard>,
    val discardStack: List<WagonCard> = emptyList(),

    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val endPlayer: Player? = null,

    val cities: List<City>,
) {
    val currentPlayer: Player
        get() = players[currentPlayerIndex]
}
