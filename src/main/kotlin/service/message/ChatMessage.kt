package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * Message used to send arbitrary text to other clients. Used as a chat for
 * communication between players.
 */
@GameActionClass
data class ChatMessage (
    val text: String
) : GameAction() {
    override fun toString(): String {
        return "[Chat] $text"
    }
}
