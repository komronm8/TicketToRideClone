package service

import entity.AIPlayer
import service.message.*
import tools.aqua.bgw.util.Stack
import java.io.File
import java.io.InputStream


class NetworkService(val rootService: RootService): AbstractRefreshingService() {
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
        updateConnectionState(ConnectionState.CONNECTED)

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
        updateConnectionState(ConnectionState.CONNECTED)

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
    fun startNewHostedGame(playerNames: List<String>) {
        check(connectionState == ConnectionState.WAIT_FOR_PLAYERS)
        { "currently not prepared to start a new hosted game." }

        val playerData: MutableList<GameService.PlayerData> = mutableListOf()
        playerNames.forEach {name ->
            playerData.add(GameService.PlayerData(name,true))
        }

        rootService.gameService.startNewGame(playerData.toList())
        val game = rootService.game.currentState

        val colors = Stack(PlayerColor.RED, PlayerColor.WHITE, PlayerColor.PURPLE)

        val message = GameInitMessage(
            game.wagonCardsStack.map { it.color.maptoMessageColor() },
            game.players.map { player -> Player(isBot = player is AIPlayer,
                trainCards = player.wagonCards.map { it.color.maptoMessageColor() },
                color = colors.pop(),
                destinationTickets = player.destinationCards.map {
                    DestinationTicket(it.points, mapToCityEnum(readIdentifierFromCSV(it.cities.first.name, false)),
                        mapToCityEnum(readIdentifierFromCSV(it.cities.second.name, false))) }) },
            game.destinationCards.map {
                DestinationTicket(it.points, mapToCityEnum(readIdentifierFromCSV(it.cities.first.name, false)),
                    mapToCityEnum(readIdentifierFromCSV(it.cities.second.name, false))) })
        
        updateConnectionState(ConnectionState.PLAY_TURN)
        client?.sendGameActionMessage(message)

    }


    /**
     * send a [DebugMessage]
     *
     * @throws when not connected to a game
     */
    fun sendDebugMessage() {
        check(connectionState != ConnectionState.DISCONNECTED) { "Not connected to a game" }

        val numOfDestinationCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach {p ->
            numOfDestinationCards.add(p.destinationCards.size)
        }

        val numOfTrainCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach {p ->
            numOfTrainCards.add(p.trainCarsAmount)
        }

        val numOfClaimedRoutes: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach {p ->
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

    fun receiveDebugResponseMessage(message: DebugMessage) {
        check(connectionState != ConnectionState.DISCONNECTED) { "Not connected to a game" }
        var consistent: Boolean = true

        val numOfDestinationCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach {p ->
            numOfDestinationCards.add(p.destinationCards.size)
        }

        val numOfTrainCards: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach {p ->
            numOfTrainCards.add(p.trainCarsAmount)
        }

        val numOfClaimedRoutes: MutableList<Int> = mutableListOf()
        rootService.game.currentState.players.forEach {p ->
            numOfClaimedRoutes.add(p.claimedRoutes.size)
        }

        val trainCardStackCount: Int = rootService.game.currentState.wagonCardsStack.size

        if(numOfDestinationCards.toList() != message.numOfDestinationCards || numOfTrainCards.toList() != message.numOfTrainCards
            || numOfClaimedRoutes.toList() != message.numOfClaimedRoutes || trainCardStackCount != message.trainCardStackCount){
            updateConnectionState(ConnectionState.ERROR)
            consistent = false
        }

        val message = DebugResponseMessage(consistent)

        client?.sendGameActionMessage(message)

    }

    fun sendDrawDestinationTicket(selectedDestinationTickets: List<DestinationTicket>){

    }

    data class CityMapping(
        val identifier: String,
        val cityName: String
    )
    private fun readCsvAndSearchName(inputStream: InputStream, cityNameToFind: String): String{
        val reader = inputStream.bufferedReader()
        val values = reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (cityCode, cityName) = it.split(',', ignoreCase = false, limit = 2)
                CityMapping(cityCode,cityName)
            }.toList()
        val filtVal = values.filter { it.cityName == cityNameToFind }
        if(filtVal.isEmpty()){
            return ""
        }else {
            return filtVal[0].identifier
        }
    }

    private fun readCsvAndSearchIdentifier(inputStream: InputStream, cityNameToFind: String): String{
        val reader = inputStream.bufferedReader()
        val values = reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (cityCode, cityName) = it.split(',', ignoreCase = false, limit = 2)
                CityMapping(cityCode,cityName)
            }.toList()
        val filtVal = values.filter { it.identifier == cityNameToFind }
        if(filtVal.isEmpty()){
            return ""
        }else {
            return filtVal[0].identifier
        }
    }

    /**
     *
     */
    fun readIdentifierFromCSV(cityName: String, isIdentifier: Boolean): String{
        var fileName = "/City_Enum_Zuordnung_1.csv"
        println(NetworkService::class.java.getResource(fileName))
        var file = File(NetworkService::class.java.getResource(fileName).file)
        if(isIdentifier){
            return readCsvAndSearchIdentifier(file.inputStream(), cityName)
        }else {
            return readCsvAndSearchName(file.inputStream(), cityName)
        }
    }

    fun sendChatMessage(text: String){
        client?.sendGameActionMessage(ChatMessage(text))
    }

    fun mapToCityEnum(str: String): City{
        when(str){
            "ALB" ->return City.ALB;"AND" ->return City.AND;"ARH" ->return City.ARH;"BER"->return City.BER;"BOD"->return City.BOD;
            "GOT"->return City.GOT;
            "HEL"->return City.HEL;"HON"->return City.HON;"IMA"->return City.IMA;
        "KAJ" ->return City.KAJ;"KAR"->return City.KAR;"KIK" ->return City.KIK;"KIR" ->return City.KIR;"KOB" ->return City.KOB;
            "KRI" ->return City.KRI;"KUO" ->return City.KUO;
            "LAU" ->return City.LAU;"LIE" ->return City.LIE;
        "LIL" ->return City.LIL;"MOR" ->return City.MOR;"MUR" ->return City.MUR;"NAR" ->return City.NAR;"NOR" ->return City.NOR;
            "ORE"->return City.ORE;"OSL"->return City.OSL;
            "OST"->return City.OST;"OUL"->return City.OUL;
        "ROV" ->return City.ROV;"STA"->return City.STA;"STO"->return City.STO;"SUN"->return City.SUN;"TAL"->return City.TAL;"TAM"->return City.TAM;"TOR"->return City.TOR;
            "TRO"->return City.TRO;"TRH"->return City.TRH;
        "TUR"->return City.TUR;"UME"->return City.UME;"VAA"->return City.VAA;
            else -> throw IllegalArgumentException("$str not in enum.")
        }
    }
}
