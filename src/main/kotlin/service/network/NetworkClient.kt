package service

import entity.*
import entity.City
import service.network.NetworkService
import service.network.message.*
import service.network.message.Color
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import kotlin.math.max


/**
 * Handels incoming BGW-Net messages
 *
 * @property playerName Name of the Player
 * @property host Name of the Host
 * @property secret Verificationcode for the Server
 */
class NetworkClient(
    playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService,
) : BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {
    /** the identifier of this game session; can be null if no session started yet. */
    var sID: String? = null

    /** the name of the opponent player; can be null if no message from the opponent received yet */
    var playersNames: MutableList<String> = mutableListOf()
    var clientAI: AIPlayer.Strategy? = null

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
                    networkService.onAllRefreshables { refreshAfterPlayerJoin() }
                }

                CreateGameResponseStatus.SESSION_WITH_ID_ALREADY_EXISTS -> {
                    networkService.disconnect()
                    networkService.onAllRefreshables { refreshAfterError("SID EXISTS") }
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
                    networkService.onAllRefreshables { refreshAfterPlayerJoin() }
                }

                JoinGameResponseStatus.INVALID_SESSION_ID -> {
                    networkService.disconnect()
                    networkService.onAllRefreshables { refreshAfterError("INVALID SID") }
                }

                JoinGameResponseStatus.PLAYER_NAME_ALREADY_TAKEN -> {
                    networkService.disconnect()
                    networkService.onAllRefreshables { refreshAfterError("PLAYER NAME EXISTS") }
                }

                else -> disconnectAndError(response.status)
            }
        }
    }

    /**
     * Handle Errors and sends an Error message
     */
    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        playersNames = mutableListOf()
        error(message)
    }

    /**
     * Returns the [City] for the [City] Enum [toString]
     */
    fun getCity(name: String): City {
        return networkService.rootService.game.currentState.cities.first {
            it.name == networkService.readIdentifierFromCSV(
                name,
                true
            )
        }
    }

    /**
     * Returns the [Route] for the two [City] Enum [toString] and the [Color]
     */
    fun getRoute(nameStart: String, nameEnd: String, color: Color): Route {
        return getCity(nameStart).routes.first {
            (it.cities.first == getCity(nameEnd) || it.cities.second == getCity(nameEnd))
                    && it.color == color.maptoGameColor()
        }
    }

    /**
     * Handel a [ChatMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onChatMessageReceivedAction(message: ChatMessage, sender: String) {
        BoardGameApplication.runOnGUIThread { networkService.onAllRefreshables { refreshAfterText("[$sender] $message") } }
    }

    /**
     * Handel a [DrawTrainCardMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onDrawTrainCardMessageReceivedAction(message: DrawTrainCardMessage, sender: String) {
        if (message.newTrainCardStack != null) {
            networkService.rootService.insert(networkService.rootService.game.currentState.copy(
                wagonCardsStack = networkService.rootService.game.currentState.wagonCardsStack
                        + message.newTrainCardStack.map { WagonCard(it.maptoGameColor()) }
            ))
        }
        message.selectedTrainCards.forEach { color: Color ->
            networkService.rootService.playerActionService.drawWagonCard(
                networkService.rootService.game.currentState.openCards.indexOf(WagonCard(color.maptoGameColor()))
            )
        }
        if (networkService.rootService.game.currentState.currentPlayer.name == playerName) {
            networkService.updateConnectionState(ConnectionState.PLAY_TURN)
        }
    }

    /**
     * Handel a [DrawDestinationTicketMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onDrawDestinationTicketMessageReceivedAction(message: DrawDestinationTicketMessage, sender: String) {
        val destinationIndices = message.selectedDestinationTickets
            .map { card: DestinationTicket-> networkService.rootService.game.currentState.destinationCards
                .indexOfFirst { it.points == card.score && it.cities.first == getCity(card.start.toString())
                        && it.cities.second == getCity(card.end.toString()) }
            }
            .map {
                it - max(networkService.rootService.game.currentState.destinationCards.size - 3, 0)
            }
        networkService.rootService.playerActionService.drawDestinationCards(destinationIndices)
        if (networkService.rootService.game.currentState.currentPlayer.name == playerName) {
            networkService.updateConnectionState(ConnectionState.PLAY_TURN)
        }
    }

    /**
     * Handel a [ClaimARouteMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onClaimARouteMessageReceivedAction(message: ClaimARouteMessage, sender: String) {
        val route = getRoute(message.start.toString(), message.end.toString(), message.railColor)
        val sibling = route.sibling
        val expectedWagonCards = message.playedTrainCards.map { WagonCard(it.maptoGameColor()) }
        val currentState = networkService.rootService.game.currentState
        val playerCards = currentState.currentPlayer.wagonCards.groupBy { it.color }
        val expectedCount = expectedWagonCards.groupBy { it.color }.mapValues { it.value.count() }
        val wagonCards = expectedCount.flatMap { checkNotNull(playerCards[it.key]).subList(0, it.value) }
        if (networkService.rootService.playerActionService.claimRoute(
                route, wagonCards
            )
            == PlayerActionService.ClaimRouteFailure.RouteAlreadyClaimed && sibling != null
        ) {
            networkService.rootService.playerActionService.claimRoute(
                sibling, message.playedTrainCards.map { WagonCard(it.maptoGameColor()) })
        }
        if (networkService.rootService.game.currentState.currentPlayer.name == playerName) {
            networkService.updateConnectionState(ConnectionState.PLAY_TURN)
        }

    }

    /**
     * Handel a [DebugMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onDebugMessageReceivedAction(message: DebugMessage, sender: String) {
        networkService.sendDebugResponseMessage(message)
    }

    /**
     * Handel a [DebugResponseMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onDebugMessageResponseReceivedAction(message: DebugResponseMessage, sender: String) {
        if (!message.consistent) {
            error(disconnectAndError(message))
        }
    }

    /**
     * Handel a [GameInitMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onGameInitMessageReceived(message: GameInitMessage, sender: String) {
        check(networkService.connectionState == ConnectionState.WAIT_FOR_GAMEINIT) { "Wrong State" }
        networkService.updateConnectionState(ConnectionState.BUILD_GAMEINIT_RESPONSE)
        val cities = constructGraph()

        val players = message.players.zip(playersNames).map { player ->
            val destinationCards = player.first.destinationTickets.map { card: DestinationTicket ->
                DestinationCard(
                    card.score, Pair(cities.first {
                        networkService.readIdentifierFromCSV(card.start.toString(), true) == it.name
                    },
                        cities.first { networkService.readIdentifierFromCSV(card.end.toString(), true) == it.name })
                )
            }
            val wagonCards = player.first.trainCards.map { WagonCard(it.maptoGameColor()) }
            val isRemote = player.second != playerName
            val clientAI = clientAI
            if (!isRemote && clientAI != null) {
                entity.AIPlayer(
                    name = player.second,
                    destinationCards = destinationCards,
                    wagonCards = wagonCards,
                    strategy = clientAI
                )
            } else {
                entity.Player(
                    name = player.second,
                    destinationCards = destinationCards,
                    wagonCards = wagonCards,
                    isRemote = isRemote
                )
            }
        }

        networkService.rootService.game = Game(
            State(
                destinationCards = message.destinationTickets.map { card ->
                    DestinationCard(
                        card.score, Pair(cities.first {
                            networkService.readIdentifierFromCSV(card.start.toString(), true) == it.name
                        },
                            cities.first { networkService.readIdentifierFromCSV(card.end.toString(), true) == it.name })
                    )
                },
                cities = cities,
                players = players,
                openCards = message.trainCardStack.map { WagonCard(it.maptoGameColor()) }.subList(0, 5),
                wagonCardsStack = message.trainCardStack.map { WagonCard(it.maptoGameColor()) }
                    .subList(5, message.trainCardStack.size),
            )
        )
        networkService.updateConnectionState(ConnectionState.BUILD_GAMEINIT_RESPONSE)
        networkService.rootService.game.gameState = GameState.CHOOSE_DESTINATION_CARD

        //networkService.sendDebugMessage()
        BoardGameApplication.runOnGUIThread { networkService.onAllRefreshables { refreshAfterStartNewGame() } }
    }

    /**
     * Handel a [GameInitResponseMessage] sent by the Server
     */
    @GameActionReceiver
    private fun onGameInitResponseMessageReceived(message: GameInitResponseMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.rootService.gameService.chooseDestinationCards(sender,
                message.selectedDestinationTickets.map { card: DestinationTicket ->
                    networkService.rootService.game.currentState.players.first { it.name == sender }
                        .destinationCards.indexOfFirst {
                            it.cities.toList().containsAll(
                                listOf(getCity(card.start.name), getCity(card.end.name))
                            ) && it.points == card.score
                        }
                })
        }
    }

    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        super.onPlayerJoined(notification)
        check(networkService.connectionState == ConnectionState.WAIT_FOR_PLAYERS) { "Wrong State" }
        playersNames += notification.sender
        if (playersNames.size !in 1..3) {
            networkService.updateConnectionState(ConnectionState.ERROR)
        }
        BoardGameApplication.runOnGUIThread { networkService.onAllRefreshables { refreshAfterPlayerJoin() } }
    }

    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        super.onPlayerLeft(notification)
        playersNames.remove(notification.sender)
        if (playersNames.size in 1..3) {
            networkService.updateConnectionState(ConnectionState.WAIT_FOR_PLAYERS)
        }
        BoardGameApplication.runOnGUIThread { networkService.onAllRefreshables { refreshAfterPlayerDisconnect() } }
    }

}