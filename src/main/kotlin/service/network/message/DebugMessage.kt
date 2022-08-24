package service.network.message

import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionClass

/**
 * The debug message is used to check if all players are still in "sync" and if not other differences occurred on the
 * different machines. The other clients must answer this message with the [DebugResponseMessage].
 * @param numOfDestinationCards List of Integers, which holds the quantity of the destination Cards from every player.
 * @param numOfTrainCards List of Integers, which holds the quantity of the train Cards from every player.
 * @param numOfClaimedRoutes List of Integers, which holds the quantity of claimed routes from every player.
 * @param trainCardStackCount is one Integer, which stores the current number of cards on the trainCardStack.
 */
@GameActionClass
data class DebugMessage(
    val numOfDestinationCards: List<Int>,
    val numOfTrainCards: List<Int>,
    val numOfClaimedRoutes: List<Int>,
    val trainCardStackCount: Int,
) : GameAction() {


    override fun toString(): String {
        return "[Debug] $numOfDestinationCards, $numOfTrainCards, $numOfClaimedRoutes, $trainCardStackCount"
    }
}