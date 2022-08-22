package service

import entity.*
import entity.City
import service.message.*
import service.message.Color
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.*
import kotlin.math.min


class NetworkClient(playerName: String,
                    host: String,
                    secret: String,
                    var networkService: NetworkService,
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {
    /** the identifier of this game session; can be null if no session started yet. */
    var sID: String? = null

    /** the name of the opponent player; can be null if no message from the opponent received yet */
    var playersNames: MutableList<String?> = mutableListOf()
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
                    playersNames += playerName
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
                    if (playersNames.isEmpty()) {
                        playersNames.addAll(response.opponents)
                        playersNames.add(playerName)
                    }
                    networkService.updateConnectionState(ConnectionState.WAIT_FOR_GAMEINIT)
                }
                else -> disconnectAndError(response.status)
            }
            println(playersNames)
        }
    }

    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        playersNames = mutableListOf()
        error(message)
    }

    fun getCity(name: String): City{
        return networkService.rootService.game.currentState.cities.first { it.name == networkService.readIdentifierFromCSV(name, true) }
    }

    fun getRoute(nameStart: String, nameEnd:String, color: Color): Route{
         return getCity(nameStart).routes.first {
             (it.cities.first == getCity(nameEnd) || it.cities.second == getCity(nameEnd))
                     && it.color == color.maptoGameColor()
         }
    }

    @GameActionReceiver
    private fun onChatMessageReceivedAction(message: ChatMessage, sender: String){
        println("[CHAT] $sender: $message")
    }

    @GameActionReceiver
    private fun onDrawTrainCardMessageReceivedAction(message: DrawTrainCardMessage, sender: String){
        if (message.newTrainCardStack != null) {
            networkService.rootService.insert(networkService.rootService.game.currentState.copy(
                wagonCardsStack = networkService.rootService.game.currentState.wagonCardsStack
                        + message.newTrainCardStack.map { WagonCard(it.maptoGameColor()) }
            ))
        }
        message.color.forEach { color: Color -> networkService.rootService.playerActionService.drawWagonCard(
            networkService.rootService.game.currentState.openCards.indexOf(WagonCard(color.maptoGameColor()))
        ) }
    }

    @GameActionReceiver
    private fun onDrawDestinationTicketMessageReceivedAction(message: DrawDestinationTicketMessage, sender: String){
        val cards = networkService.rootService.game.currentState.destinationCards.subList(0, 2)
        val ints: MutableList<Int> = mutableListOf()
        for (i in 0 until min(3, networkService.rootService.game.currentState.destinationCards.size)){
            if (message.selectedDestinationTickets.any {
                    cards[i].points == it.score &&
                            (cards[i].cities == Pair(getCity(it.start.toString()), getCity(it.end.toString())) ||
                                    cards[i].cities == Pair(getCity(it.end.toString()), getCity(it.start.toString()))) }) {
                ints += i
            }
        }
        networkService.rootService.playerActionService.drawDestinationCards(ints)
    }

    @GameActionReceiver
    private fun onClaimARouteMessageReceivedAction(message: ClaimARouteMessage, sender: String){
        networkService.rootService.playerActionService.claimRoute(
            getRoute(message.start.toString(), message.end.toString(), message.color),
            message.playedTrainCards.map { WagonCard(it.maptoGameColor()) }
        )
    }

    @GameActionReceiver
    private fun onDebugResponseMessageReceivedAction(message: DebugMessage, sender: String){
        println(message.toString())
    }

    @GameActionReceiver
    private fun onGameInitMessageReceived(message: GameInitMessage, sender: String){

        val cities = constructGraph()

        val players = message.players.map { player ->
            entity.Player(name = "Test", destinationCards = player.destinationTickets.map {
            DestinationCard(it.score, Pair(getCity(it.start.toString()), getCity(it.end.toString()))) },
                wagonCards = message.players.map { WagonCard(it.color.maptoGameColor()) }, isRemote = true)
            }

        networkService.rootService.insert(State(
            destinationCards = message.destinationTickets.map { DestinationCard(it.score, Pair(getCity(it.start.toString()), getCity(it.end.toString()))) },
            cities = cities, players = players, openCards = message.trainCardStack.map { WagonCard(it.maptoGameColor()) }.subList(0,5),
            wagonCardsStack = message.trainCardStack.map { WagonCard(it.maptoGameColor()) }.subList(5, message.trainCardStack.size)))

        networkService.rootService.game.gameState = GameState.CHOOSE_DESTINATION_CARD
    }

    @GameActionReceiver
    private fun onPlayerNotification(message: PlayerJoinedNotification, sender: String) {
        playersNames += message.sender
        networkService.onAllRefreshables { refreshAfterPlayerJoin() }
    }

    @GameActionReceiver
    private fun onPlayerLeftNotification(message: PlayerLeftNotification, sender: String) {
        playersNames.remove(sender)
        networkService.onAllRefreshables { refreshAfterPlayerDisconnect() }
    }
}