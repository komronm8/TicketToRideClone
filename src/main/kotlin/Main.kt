import entity.AIPlayer
import service.ai.AIService

fun main() {
    var montyWon = 0
    repeat(30) {
        val result = AIService.runAppropriate()
        if (result is AIPlayer && result.strategy is AIPlayer.Strategy.MonteCarlo) {
            montyWon += 1
        }
        println("wr: ${montyWon / (it.toDouble() + 1)}")
    }
    println(montyWon)
    //println(Optimizer().optimizeC(25))
    println("Application ended. Goodbye")
}