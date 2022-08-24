package service.network.message

/**
 * Object used to represent a single destination ticket,
 * used in [GameInitMessage], [GameInitResponseMessage]
 * and [DrawDestinationTicketMessage]
 * @param score the amount of points the destination ticket is worth
 * @param start the start city of the destination ticket
 * @param end the end city of the destination ticket
 */
data class DestinationTicket (
    val score: Int,
    val start: City,
    val end: City,
)