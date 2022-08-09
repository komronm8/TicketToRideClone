package entity

data class Player(
    val points: Int,
    val name: String,
    val destinationCards: List<DestinationCard>,
    val wagonCards: List<WagonCard>,
    val trainCarsAmount: Int
)