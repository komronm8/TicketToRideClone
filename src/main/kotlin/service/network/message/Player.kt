package service.network.message

/**
 * Object used to represent a single player,
 * used in [GameInitMessage] to synchronize
 * player data between clients
 * @param isBot whether the player is computer-controlled or human
 * @param trainCards the train cards the player gets on game start
 * @param color the color of the player
 * @param destinationTickets the five destination tickets the player gets
 * on game start, the player must keep 2-5 of them and can discard the rest
 */
data class Player (
    val isBot: Boolean,
    val trainCards: List<Color>,
    val color: PlayerColor,
    val destinationTickets: List<DestinationTicket>
)