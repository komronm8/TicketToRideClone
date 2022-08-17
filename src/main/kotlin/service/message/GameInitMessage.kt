package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Game Initialization Message is a Message containing the trainCardStack, players and destinationTickets the clients need
 * to initialize their Game State.
 * Upon receiving the GameInitMessage from the Host, the Client should initialize his Entity layer with the contained Information
 * @param trainCardStack List of Colors representing TrainCards
 * @param players List of Player-Objects
 * @param destinationTickets List of DestinationTicket-Objects
 * @throws IllegalArgumentException if Invalid Number of Arguments are given
 */

@GameActionClass
data class GameInitMessage (
    val trainCardStack: List<Color>,
    val players: List<Player>,
    val destinationTickets: List<DestinationTicket>
) : GameAction(){
    override fun toString(): String {
        return "[GameInit] $trainCardStack, $players, $destinationTickets"
    }



    init{
        require(destinationTickets.size == 31 || destinationTickets.size == 36){ "invalid number of Destination Tickets" }
        require(players.size == 2 || players.size == 3){ "invalid Number of Players" }
        require(trainCardStack.size == 98 || trainCardStack.size == 102){ "invalid number of TrainCards" }
    }
}