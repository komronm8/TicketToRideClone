package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class ClaimARouteMessage (
    val end: City,
    val start: City,
    val newTrainCardStack: List<Color>?,
    val playedTrainCards: List<Color>,
    val railColor: Color,
    val drawnTunnelCards: List<Color>?
) : GameAction() {
    override fun toString(): String {
        return "[ClaimARoute] From $start to $end"
    }
}