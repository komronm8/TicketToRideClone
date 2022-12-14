package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Message used to select the destination tickets sent in the GameInitMessage.
 * Players may only send this on their turn.
 */
@GameActionClass
data class GameInitResponseMessage (
    val selectedDestinationTickets: List<DestinationTicket>,
    val isGameInitResponse: Boolean
) : GameAction() {
    override fun toString(): String {
        return "[GameInitResponse:$isGameInitResponse] $selectedDestinationTickets"
    }


    init{
        require(selectedDestinationTickets.size in 2..5 && isGameInitResponse){"invalid amount of DestinationTickets"}
        require(isGameInitResponse)
    }
}
