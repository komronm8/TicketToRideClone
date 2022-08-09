package entity

import tools.aqua.bgw.util.Stack

data class Game(
    val destinationCards: Stack<DestinationCard>,

    val openCards: List<WagonCard>,
    val wagonCardsStack: Stack<WagonCard>,
    val discardStack: Stack<WagonCard> = Stack(),

    val players: List<Player>,
    val currentPlayer: Int = 0,
    val endPlayer: Player? = null,

    val cities: List<City>
)
