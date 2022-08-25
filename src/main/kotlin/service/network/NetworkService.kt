package service.network

import entity.*
import service.AbstractRefreshingService
import service.ConnectionState
import service.NetworkClient
import service.RootService
import service.network.message.*
import tools.aqua.bgw.util.Stack
import java.io.InputStream
import service.network.message.City as MessageCity
import service.network.message.Color as MessageColor
import service.network.message.Player as RemotePlayer

/**
 * Handels gamestate for BGW-Net
 *
 * @property root Enables access to GUI, Service and Entity
 */
class NetworkService(val rootService: RootService) : AbstractRefreshingService() {
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net-test/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "TicketToRide"
    }

    /** Network client. Nullable for offline games. */
    var client: NetworkClient? = null
        private set

    /**
     * current state of the connection in a network game.
     */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /**
     * Connects to server and creates a new game session.
     *
     * @param secret Server secret.
     * @param name Player name.
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(secret: String, name: String, sessionID: String?) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }

        if (sessionID.isNullOrBlank()) {
            client?.createGame(GAME_ID, "Welcome!")
        } else {
            client?.createGame(GAME_ID, sessionID, "Welcome!")
        }
        updateConnectionState(ConnectionState.WAIT_FOR_HOST_CONFIRMATION)
    }

    /**
     * Connects to server, sets the [NetworkService.client] if successful and returns `true` on success.
     *
     * @param secret Network secret. Must not be blank (i.e. empty or only whitespaces)
     * @param name Player name. Must not be blank
     *
     * @throws IllegalArgumentException if secret or name is blank
     * @throws IllegalStateException if already connected to another game
     */
    fun connect(secret: String, name: String): Boolean {
        require(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(secret.isNotBlank()) { "server secret must be given" }
        require(name.isNotBlank()) { "player name must be given" }

        val newClient =
            NetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = secret,
                networkService = this
            )

        return if (newClient.connect()) {
            this.client = newClient
            updateConnectionState(ConnectionState.CONNECTED)
            true
        } else {
            false
        }
    }

    /**
     * Connects to server and joins a game session as guest player.
     *
     * @param secret Server secret.
     * @param name Player name.
     * @param sessionID identifier of the joined session (as defined by host on create)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame(secret: String, name: String, sessionID: String) {
        if (!connect(secret, name)) {
            error("Connection failed")
        }

        val game = client?.joinGame(sessionID, "Hello!")
        game.toString()
        updateConnectionState(ConnectionState.WAIT_FOR_JOIN_CONFIRMATION)
    }

    /**
     * Updates the [connectionState] to [newState] and notifies
     * all refreshables via [Refreshable.refreshConnectionState]
     */
    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
        /**
        onAllRefreshables {
        refreshConnectionState(newState)
        }
         */
    }

    /**
     * Disconnects the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active.
     */
    fun disconnect() {
        client?.apply {
            if (sID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * set up the game using [GameService.startNewGame] and send the game init message
     * to the guest player. [connectionState] needs to be [ConnectionState.WAITING_FOR_GUEST].
     * This method should be called from the [WarNetworkClient] when the guest joined notification
     * arrived. See [WarNetworkClient.onPlayerJoined].
     *
     * @param hostPlayerName player name of the host player
     * @param guestPlayerName player name of the guest player
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_GUEST]
     */
    fun startNewHostedGame(game: State) {
        check(connectionState == ConnectionState.WAIT_FOR_PLAYERS)
        { "currently not prepared to start a new hosted game." }
        /**
        val playerData: MutableList<GameService.PlayerData> = mutableListOf()
        playerNames.forEach {name ->
        playerData.add(GameService.PlayerData(name,true))
        }

        rootService.gameService.startNewGame(playerData.toList())
        val game = rootService.game.currentState
         */
        val colors = Stack(PlayerColor.RED, PlayerColor.WHITE, PlayerColor.PURPLE)

        val cards = (game.openCards + game.wagonCardsStack).map { it.color.maptoMessageColor() }
        val players = game.players.map { player ->
            RemotePlayer(isBot = player is AIPlayer,
                trainCards = player.wagonCards.map { it.color.maptoMessageColor() },
                color = colors.pop(),
                destinationTickets = player.destinationCards.map {
                    DestinationTicket(
                        it.points, mapToCityEnum(readIdentifierFromCSV(it.cities.first.name, false)),
                        mapToCityEnum(readIdentifierFromCSV(it.cities.second.name, false))
                    )
                })
        }
        val message = GameInitMessage(
            trainCardStack = cards,
            players = players,
            game.destinationCards.map {
                DestinationTicket(
                    it.points, mapToCityEnum(readIdentifierFromCSV(it.cities.first.name, false)),
                    mapToCityEnum(readIdentifierFromCSV(it.cities.second.name, false))
                )
            })

        updateConnectionState(ConnectionState.BUILD_GAMEINIT_RESPONSE)
        client?.sendGameActionMessage(message)

        //sendDebugMessage()

    }


    /**
     * send a [DebugMessage]
     *
     * @throws IllegalStateException when not connected to a game
     */
    fun sendDebugMessage() {
        check(connectionState != ConnectionState.DISCONNECTED) { "Not connected to a game" }

        val numOfDestinationCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach { p ->
            numOfDestinationCards.add(p.destinationCards.size)
        }

        val numOfTrainCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach { p ->
            numOfTrainCards.add(p.wagonCards.size)
        }

        val numOfClaimedRoutes: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach { p ->
            numOfClaimedRoutes.add(p.claimedRoutes.size)
        }

        val trainCardStackCount: Int = rootService.game.currentState.wagonCardsStack.size

        val message = DebugMessage(
            numOfDestinationCards.toList(),
            numOfTrainCards.toList(),
            numOfClaimedRoutes.toList(),
            trainCardStackCount
        )

        client?.sendGameActionMessage(message)
    }

    /**
     * Send a [DebugResponseMessage]
     *
     * @param message The [DebugMessage] it responds to
     *
     * @throws IllegalStateException when not connected to a game
     */
    fun sendDebugResponseMessage(message: DebugMessage) {
        check(connectionState != ConnectionState.DISCONNECTED) { "Not connected to a game" }
        var consistent: Boolean = true

        val numOfDestinationCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach { p ->
            numOfDestinationCards.add(p.destinationCards.size)
        }

        val numOfTrainCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach { p ->
            numOfTrainCards.add(p.wagonCards.size)
        }

        val numOfClaimedRoutes: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach { p ->
            numOfClaimedRoutes.add(p.claimedRoutes.size)
        }

        val trainCardStackCount: Int = rootService.game.currentState.wagonCardsStack.size

        if (numOfDestinationCards.toList() != message.numOfDestinationCards
            || numOfTrainCards.toList() != message.numOfTrainCards
            || numOfClaimedRoutes.toList() != message.numOfClaimedRoutes
            || trainCardStackCount != message.trainCardStackCount
        ) {
            updateConnectionState(ConnectionState.ERROR)
            consistent = false
        }

        val message = DebugResponseMessage(consistent)

        client?.sendGameActionMessage(message)

    }

    /**
     * Send a [DrawDestinationTicketMessage]
     *
     * @param selectedDestinationTickets The selected [DestinationCard]s
     *
     * @throws IllegalStateException If it is not your turn
     */
    fun sendDrawDestinationTicket(selectedDestinationTickets: List<DestinationCard>) {
        check(connectionState == ConnectionState.PLAY_TURN) { "Not in a state to send Draw Destination ticket" }

        val tmp: MutableList<DestinationTicket> = mutableListOf()
        selectedDestinationTickets.forEach {
            tmp.add(
                DestinationTicket(
                    it.points, mapToCityEnum(readIdentifierFromCSV(it.cities.first.name, false)),
                    mapToCityEnum(readIdentifierFromCSV(it.cities.second.name, false))
                )
            )
        }

        val message = DrawDestinationTicketMessage(tmp.toList())
        client?.sendGameActionMessage(message)
        updateConnectionState(ConnectionState.WAIT_FOR_TURN)
    }

    /**
     * Send a [gameInitResponseMessage]
     *
     * @param selectedCards The [DestinationCard]s selected at the Beginning of the Game
     *
     * @throws IllegalStateException If the Game has not been initialised
     */
    fun gameInitResponseMessage(selectedCards: List<DestinationCard>) {
        check(connectionState == ConnectionState.BUILD_GAMEINIT_RESPONSE) { "Not in a state to send GameInitResponse" }

        val tmp: MutableList<DestinationTicket> = mutableListOf()
        selectedCards.forEach {
            tmp.add(
                DestinationTicket(
                    it.points, mapToCityEnum(readIdentifierFromCSV(it.cities.first.name, false)),
                    mapToCityEnum(readIdentifierFromCSV(it.cities.second.name, false))
                )
            )
        }

        val message = GameInitResponseMessage(tmp.toList(), isGameInitResponse = true)
        client?.sendGameActionMessage(message)
        updateConnectionState(ConnectionState.WAIT_FOR_GAMEINIT_RESPONSE)
    }

    /**
     * Send a [DrawTrainCardMessage]
     *
     * @param selectedTrainCards The selected [DestinationCard]s
     *
     * @throws IllegalStateException If it is not your turn
     */
    fun sendDrawTrainCardMessage(selectedTrainCards: List<WagonCard>, newTrainCardStack: List<WagonCard>?) {
        check(connectionState == ConnectionState.PLAY_TURN)
        { "Expected State: PLAY_TURN, Gotten: $connectionState" }
        val tmpWC: List<MessageColor> = selectedTrainCards.map {
            it.color.maptoMessageColor()
        }
        if (newTrainCardStack != null) {
            val tmpNewStack: List<MessageColor> = newTrainCardStack.map {
                it.color.maptoMessageColor()
            }
            val message = DrawTrainCardMessage(tmpWC, tmpNewStack)
            client?.sendGameActionMessage(message)
        } else {
            val message = DrawTrainCardMessage(tmpWC, null)
            client?.sendGameActionMessage(message)
        }
        updateConnectionState(ConnectionState.WAIT_FOR_TURN)
    }

    /**
     * Send a [ClaimARouteMessage]
     *
     * @param claimedRoute The claimed [Route]
     * @param newTrainCardStack If the DiscardStack has een shuffled
     * @param playedTrainCards The [WagonCard]s used to claim the [Route]
     * @param drawnTunnelCards If the [Route] is a [Tunnel] the additional [WagonCard]
     *
     * @throws IllegalStateException If it is not your turn
     */
    fun sendClaimARounteMessage(
        claimedRoute: Route, newTrainCardStack: List<WagonCard>?,
        playedTrainCards: List<WagonCard>, drawnTunnelCards: List<WagonCard>?
    ) {
        println(rootService.game.currentState.currentPlayer.name)
        check(connectionState == ConnectionState.PLAY_TURN) { "Not in a state to send ClaimARounteMessage" }
        val tmpTrainCards: MutableList<MessageColor> = mutableListOf()
        playedTrainCards.forEach {
            tmpTrainCards.add(it.color.maptoMessageColor())
        }

        var tmpNewTrainCards: MutableList<MessageColor>? = mutableListOf()
        var tmpTunnelCards: MutableList<MessageColor>? = mutableListOf()

        if (newTrainCardStack != null) {
            newTrainCardStack.forEach {
                tmpNewTrainCards?.add(it.color.maptoMessageColor())
            }
        } else {
            tmpNewTrainCards = null
        }

        if (drawnTunnelCards != null) {
            drawnTunnelCards.forEach {
                tmpTunnelCards?.add(it.color.maptoMessageColor())
            }
        } else {
            tmpTunnelCards = null
        }

        val message = ClaimARouteMessage(
            mapToCityEnum(readIdentifierFromCSV(claimedRoute.cities.second.name, false)),
            mapToCityEnum(readIdentifierFromCSV(claimedRoute.cities.first.name, false)),
            tmpNewTrainCards,
            tmpTrainCards,
            claimedRoute.color.maptoMessageColor(),
            tmpTunnelCards
        )

        client?.sendGameActionMessage(message)
        updateConnectionState(ConnectionState.WAIT_FOR_TURN)
    }

    private data class CityMapping(
        val identifier: String,
        val cityName: String
    )

    private fun readCsvAndSearchName(inputStream: InputStream, cityNameToFind: String): String {
        val reader = inputStream.bufferedReader()
        val values = reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (cityCode, cityName) = it.split(',', ignoreCase = false, limit = 2)
                CityMapping(cityCode, cityName)
            }.toList()
        val filtVal = values.filter { it.cityName == cityNameToFind }
        if (filtVal.isEmpty()) {
            return ""
        } else {
            return filtVal[0].identifier
        }
    }

    private fun readCsvAndSearchIdentifier(inputStream: InputStream, cityNameToFind: String): String {
        val reader = inputStream.bufferedReader()
        val values = reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (cityCode, cityName) = it.split(',', ignoreCase = false, limit = 2)
                CityMapping(cityCode, cityName)
            }.toList()
        val filtVal = values.filter { it.identifier == cityNameToFind }
        if (filtVal.isEmpty()) {
            return ""
        } else {
            return filtVal[0].cityName
        }
    }

    /**
     * Maps the [City] to the [service.network.message.City] and in the other Direktion
     *
     * @param cityName The [String] to map
     * @param isIdentifier false -> [City] to the [service.network.message.City]
     */
    fun readIdentifierFromCSV(cityName: String, isIdentifier: Boolean): String {
        if (isIdentifier) {
            return when (cityName) {
                "ALB" -> "Ålborg"
                "AND" -> "Åndalsnes"
                "ARH" -> "Århus"
                "BER" -> "Bergen"
                "BOD" -> "Boden"
                "GOT" -> "Göteborg"
                "HEL" -> "Helsinki"
                "HON" -> "Honningsvåg"
                "IMA" -> "Imatra"
                "KAJ" -> "Kajaani"
                "KAR" -> "Karlskrona"
                "KIK" -> "Kirkenes"
                "KIR" -> "Kiruna"
                "KOB" -> "København"
                "KRI" -> "Kristiansand"
                "KUO" -> "Kuopio"
                "LAU" -> "Lahti"
                "LIE" -> "Lieksa"
                "LIL" -> "Lillehammer"
                "MOR" -> "Mo I Rana"
                "MUR" -> "Murmansk"
                "NAR" -> "Narvik"
                "NOR" -> "Norrköping"
                "ORE" -> "Örebro"
                "OSL" -> "Oslo"
                "OST" -> "Östersund"
                "OUL" -> "Oulu"
                "ROV" -> "Rovaniemi"
                "STA" -> "Stavanger"
                "STO" -> "Stockholm"
                "SUN" -> "Sundsvall"
                "TAL" -> "Tallinn"
                "TAM" -> "Tampere"
                "TOR" -> "Tornio"
                "TRO" -> "Tromsø"
                "TRH" -> "Trondheim"
                "TUR" -> "Turku"
                "UME" -> "Umeå"
                "VAA" -> "Vaasa"
                else -> throw IllegalStateException()
            }
        } else {
            return when (cityName) {
                "Ålborg" -> "ALB"
                "Åndalsnes" -> "AND"
                "Århus" -> "ARH"
                "Bergen" -> "BER"
                "Boden" -> "BOD"
                "Göteborg" -> "GOT"
                "Helsinki" -> "HEL"
                "Honningsvåg" -> "HON"
                "Imatra" -> "IMA"
                "Kajaani" -> "KAJ"
                "Karlskrona" -> "KAR"
                "Kirkenes" -> "KIK"
                "Kiruna" -> "KIR"
                "København" -> "KOB"
                "Kristiansand" -> "KRI"
                "Kuopio" -> "KUO"
                "Lahti" -> "LAU"
                "Lieksa" -> "LIE"
                "Lillehammer" -> "LIL"
                "Mo I Rana" -> "MOR"
                "Murmansk" -> "MUR"
                "Narvik" -> "NAR"
                "Norrköping" -> "NOR"
                "Örebro" -> "ORE"
                "Oslo" -> "OSL"
                "Östersund" -> "OST"
                "Oulu" -> "OUL"
                "Rovaniemi" -> "ROV"
                "Stavanger" -> "STA"
                "Stockholm" -> "STO"
                "Sundsvall" -> "SUN"
                "Tallinn" -> "TAL"
                "Tampere" -> "TAM"
                "Tornio" -> "TOR"
                "Tromsø" -> "TRO"
                "Trondheim" -> "TRH"
                "Turku" -> "TUR"
                "Umeå" -> "UME"
                "Vaasa" -> "VAA"
                else -> throw IllegalStateException("unreachable")
            }
        }
    }


    /**
     * Ssend a [ChatMessage]
     *
     * @param text The Text of the message
     */
    fun sendChatMessage(text: String) {
        client?.sendGameActionMessage(ChatMessage(text))
    }

    /**
     * Maps a [String] to the [service.network.message.City] enum
     */
    fun mapToCityEnum(str: String): MessageCity {
        when (str) {
            "ALB" -> return MessageCity.ALB;"AND" -> return MessageCity.AND;"ARH" -> return MessageCity.ARH;
            "BER" -> return MessageCity.BER;"BOD" -> return MessageCity.BOD;
            "GOT" -> return MessageCity.GOT;
            "HEL" -> return MessageCity.HEL;"HON" -> return MessageCity.HON;"IMA" -> return MessageCity.IMA;
            "KAJ" -> return MessageCity.KAJ;"KAR" -> return MessageCity.KAR;"KIK" -> return MessageCity.KIK;
            "KIR" -> return MessageCity.KIR;"KOB" -> return MessageCity.KOB;
            "KRI" -> return MessageCity.KRI;"KUO" -> return MessageCity.KUO;
            "LAU" -> return MessageCity.LAU;"LIE" -> return MessageCity.LIE;
            "LIL" -> return MessageCity.LIL;"MOR" -> return MessageCity.MOR;"MUR" -> return MessageCity.MUR;
            "NAR" -> return MessageCity.NAR;"NOR" -> return MessageCity.NOR;
            "ORE" -> return MessageCity.ORE;"OSL" -> return MessageCity.OSL;
            "OST" -> return MessageCity.OST;"OUL" -> return MessageCity.OUL;
            "ROV" -> return MessageCity.ROV;"STA" -> return MessageCity.STA;"STO" -> return MessageCity.STO;
            "SUN" -> return MessageCity.SUN;"TAL" -> return MessageCity.TAL;
            "TAM" -> return MessageCity.TAM;"TOR" -> return MessageCity.TOR;
            "TRO" -> return MessageCity.TRO;"TRH" -> return MessageCity.TRH;
            "TUR" -> return MessageCity.TUR;"UME" -> return MessageCity.UME;"VAA" -> return MessageCity.VAA;
            else -> throw IllegalArgumentException("$str not in enum.")
        }
    }

}
