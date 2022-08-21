package service

import entity.DestinationCard
import entity.WagonCard
import service.message.*
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.response.*



class NetworkClient(playerName: String,
                    host: String,
                    secret: String,
                    var networkService: NetworkService,
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {
    /** the identifier of this game session; can be null if no session started yet. */
    var sID: String? = null

    /** the name of the opponent player; can be null if no message from the opponent received yet */
    var otherPlayersNames: List<String?> = listOf()
    var test: String = ""

    /**
     * Handle a [CreateGameResponse] sent by the server. Will await the guest player when its
     * status is [CreateGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a game creation response.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAIT_FOR_HOST_CONFIRMATION)
            { "unexpected CreateGameResponse" }

            when (response.status) {
                CreateGameResponseStatus.SUCCESS -> {
                    networkService.updateConnectionState(ConnectionState.WAIT_FOR_PLAYERS)
                    sID = response.sessionID
                }
                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * Handle a [JoinGameResponse] sent by the server. Will await the init message when its
     * status is [JoinGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in NetWar, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a join game response.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAIT_FOR_JOIN_CONFIRMATION)
            { "unexpected JoinGameResponse" }

            when (response.status) {
                JoinGameResponseStatus.SUCCESS -> {
                    sID = response.sessionID
                    otherPlayersNames = response.opponents
                    networkService.updateConnectionState(ConnectionState.WAIT_FOR_GAMEINIT)
                }
                else -> disconnectAndError(response.status)
            }
        }
    }

    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        error(message)
    }

    @GameActionReceiver
    private fun onChatMessageReceivedAction(message: ChatMessage, sender: String){
        println("[CHAT] $sender: $message")
    }

    @GameActionReceiver
    private fun onDrawTrainCardMessageReceivedMessage(message: DrawTrainCardMessage, sender: String){
        message.color.forEach { color: Color -> networkService.rootService.playerActionService.drawWagonCard(
            networkService.rootService.game.currentState.openCards.indexOf(WagonCard(color.maptoGameColor()))
        ) }
        if (message.newTrainCardStack == null) {
            TODO()
        }
    }

    @GameActionReceiver
    private fun onDrawDestinationTicketMessage(message: DrawDestinationTicketMessage, sender: String){
         /*networkService.rootService.playerActionService.drawDestinationCards(message.selectedDestinationTickets.map {
             card: DestinationTicket -> networkService.rootService.game.currentState.destinationCards.indexOf(
             networkService.rootService.game.currentState.destinationCards.filter { it.cities.first.name == card.start.maptoGameCityName() }
         })*/
    }


}