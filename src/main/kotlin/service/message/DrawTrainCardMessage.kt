package service.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

@GameActionClass
data class DrawTrainCardMessage (
    val color: List<Color>,
    val newTrainCardStack: List<Color>?
) : GameAction() {
    override fun toString(): String {
        return "[DrawTrainCard] $color, $newTrainCardStack"
    }

    init {
        require(color.size == 2){ "Invalid number of train cards" }
    }
}