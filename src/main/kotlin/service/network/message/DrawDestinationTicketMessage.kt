package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Draw destination ticket message dispatches a draw destination ticket action to the server with the given
 * selected cards. It takes a list of 1 up to 3 selected destination tickets from the player.
 *
 * If client receives a draw ticket message action, it must remove three cards from its destination ticket stack.
 *
 * @param selectedDestinationTickets List of all selected destination tickets from the player.
 * Must include at least 1 and up to 3 elements
 */

@GameActionClass
data class DrawDestinationTicketMessage (
    val selectedDestinationTickets: List<DestinationTicket>
) : GameAction() {
    override fun toString(): String {
        return "[DrawDestinationTicket] $selectedDestinationTickets"
    }

    init{
        require(selectedDestinationTickets.size in 1 .. 3){"invalid amount of DestinationTickets"}
    }
}