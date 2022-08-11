package entity

import tools.aqua.bgw.util.Stack

/**
 * The game state at a given point in time.
 *
 * @param destinationCards The destination card draw stack
 * @param openCards The openly visible drawable wagon cards
 * @param wagonCardsStack The hidden wagon card draw stack
 * @param discardStack The discard stack for wagon cards
 * @param players The players
 * @param currentPlayer The index of the current player
 * @param endPlayer The first player whose [Player.trainCarsAmount] is less than two
 * @param cities The cities involved in the game
 */
data class State(
    val destinationCards: Stack<DestinationCard>,

    val openCards: List<WagonCard>,
    val wagonCardsStack: Stack<WagonCard>,
    val discardStack: Stack<WagonCard> = Stack(),

    val players: List<Player>,
    val currentPlayer: Int = 0,
    val endPlayer: Player? = null,

    val cities: List<City>
)
