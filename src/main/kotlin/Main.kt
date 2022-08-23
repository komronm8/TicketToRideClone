import entity.AIPlayer
import service.GameService
import service.ai.AIService
import service.ai.QLearning
import java.io.File


fun main() {

    if (false) {
        var netWon = 0
        for (i in 0 until 50) {
            try {
                val result = AIService.runWithAI(
                    listOf(
//                GameService.PlayerData("monty", false, AIPlayer.Strategy.MonteCarlo(500)),
                        GameService.PlayerData("randy", false, AIPlayer.Strategy.Random),
                        GameService.PlayerData("q", false, AIPlayer.Strategy.neuralNet),
                    )
                )
                if (result is AIPlayer && result.strategy is AIPlayer.Strategy.neuralNet) {
                    netWon += 1
                }
                println("wr: ${netWon / (i.toDouble() + 1)}")
            } catch (e: Exception) {
                println(e.stackTraceToString())
            }
        }
    }
    else
        while(true){
            QLearning().learn(1000,null, true)
            println("saved model")
            var netWon = 0
            val n = 50
            for (i in 0 until n) {
                try {
                    val result = AIService.runWithAI(
                        listOf(
//                GameService.PlayerData("monty", false, AIPlayer.Strategy.MonteCarlo(500)),
                            GameService.PlayerData("randy", false, AIPlayer.Strategy.Random),
                            GameService.PlayerData("q", false, AIPlayer.Strategy.neuralNet),
                        )
                    )
                    if (result is AIPlayer && result.strategy is AIPlayer.Strategy.neuralNet) {
                        netWon += 1
                    }

                } catch (_: Exception) {

                }
                println("wr: ${netWon / n.toDouble()}")
            }
            break
        }
}