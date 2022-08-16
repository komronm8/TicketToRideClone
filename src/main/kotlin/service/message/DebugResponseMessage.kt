package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * DebugResponseMessage is dispatched to the other clients as a way to signal if their current Game State is consistent with
 * the other Game States or not.
 * Upon receiving the DebugResponseMessage, the receiver should evaluate the consistent value and handle it accordingly.
 * @param consistent true if state is consistent with other players
 */


@GameActionClass
data class DebugResponseMessage (
    val consistent: Boolean
) : GameAction(){
    override fun toString(): String {
        return "[DebugResponse] $consistent"
    }
}