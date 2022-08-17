package service

import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus
import view.SopraApplication

class NetworkClient(playerName: String,
                    host: String,
                    secret: String,
                    var networkService: NetworkService,
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {
    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null

    /** the name of the opponent player; can be null if no message from the opponent received yet */
    var otherPlayerName: String? = null

    override fun onJoinGameResponse(response: JoinGameResponse) {

    }
}