import entity.AIPlayer
import service.GameService
import service.ai.AIService
import service.ai.QLearning
import java.io.File


fun main() {

//    var netWon = 0
//    repeat(10) {
//        val result = AIService.runWithAI(
//            listOf(
//                GameService.PlayerData("monty", false, AIPlayer.Strategy.MonteCarlo(500)),
//                GameService.PlayerData("randy",false, AIPlayer.Strategy.Random),
//                GameService.PlayerData("q", false, AIPlayer.Strategy.neuralNet),
//            )
//        )
//        if (result is AIPlayer && result.strategy is AIPlayer.Strategy.neuralNet) {
//            netWon += 1
//        }
//        println("wr: ${netWon / (it.toDouble() + 1)}")
//    }
    QLearning().learn(500,File("model.h5"), true)
}