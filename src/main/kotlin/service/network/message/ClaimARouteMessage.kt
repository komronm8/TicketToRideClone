package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * This message is for claiming a [entity.Route]
 *
 * @property end EndCity of the [entity.Route]
 * @property start StarCity of the [entity.Route]
 * @property railColor Color of the [entity.Route]
 * @property playedTrainCards The [entity.WagonCard]s used to claim the [entity.Route]
 * @property newTrainCardStack If the Discardstack had to be shuffled in if not null
 * @property drawnTunnelCards If additional [entity.WagonCard]s had to be used for a Tunnel
 */
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