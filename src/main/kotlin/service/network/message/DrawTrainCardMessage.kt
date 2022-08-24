package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class DrawTrainCardMessage (
    val selectedTrainCards: List<Color>,
    val newTrainCardStack: List<Color>?
) : GameAction() {
    override fun toString(): String {
        return "[DrawTrainCard] $selectedTrainCards, $newTrainCardStack"
    }

    init {
        require(selectedTrainCards.size == 2){ "Invalid number of train cards" }
    }
}