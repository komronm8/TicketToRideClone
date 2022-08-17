package service

import service.message.*
import java.io.File
import java.io.InputStream


class NetworkService(private val rootService: RootService): AbstractRefreshingService() {
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

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

        client?.joinGame(sessionID, "Hello!")

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
        val game = rootService.game
        /**
        val message = GameInitMessage(

        )
        **/
        updateConnectionState(ConnectionState.PLAY_TURN)
        //client?.sendGameActionMessage(message)
    }

    data class cityMapping(
        val identifier: String,
        val cityName: String
    )
    private fun readCsvAndSearch(inputStream: InputStream, cityNameToFind: String): String?{
        val reader = inputStream.bufferedReader()
        val values = reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (cityCode, cityName) = it.split(',', ignoreCase = false, limit = 2)
                cityMapping(cityCode,cityName)
            }.toList()
        val filt_val = values.filter { it.cityName == cityNameToFind }
        if(filt_val.isEmpty()){
            return null
        }else {
            return filt_val[0].identifier
        }
    }
    private fun readIdentifierFromCSV(cityName: String): String?{
        var fileName = "/City_Enum_Zuordnung_1.csv"
        println(NetworkService::class.java.getResource(fileName))
        var file = File(NetworkService::class.java.getResource(fileName).file)
        return readCsvAndSearch(file.inputStream(), cityName)
    }
}