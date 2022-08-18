import entity.AIPlayer
import service.ai.AIService
import service.ai.Optimizer

fun main() {
    var montyWon = 0
    repeat(10) {
        try {
            val result = AIService.runAppropriate()
            if (result is AIPlayer && result.strategy is AIPlayer.Strategy.MonteCarlo) {
                montyWon += 1
            }
            println("wr: ${montyWon / (it.toDouble()  + 1)}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    println("Application ended. Goodbye")
}