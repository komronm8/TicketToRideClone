package service.ai

import entity.AIPlayer
import service.GameService
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * A class responsible for optimizing the parameters of the monte carlo algorithm
 */
object MonteCarloOptimizer {
    /**
     * optimises for the [AIPlayer.Strategy.MonteCarlo.c] property
     */
    fun optimizeC(iterationsPerChange: Int): Double {
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        var startValue = 5.0
        var change = 2.5
        while (change > 0.005) {
            val runData = listOf(
                GameService.PlayerData("1", false, AIPlayer.Strategy.MonteCarlo(2000, startValue - change, )),
                GameService.PlayerData("2", false, AIPlayer.Strategy.MonteCarlo(2000, startValue + change)),
            )
            val tasks = List(iterationsPerChange) { Callable {
                val winner = AIService.runWithAI(runData)
                winner.name == "1"
            } }
            val oneWins = executor.invokeAll(tasks).map { it.get() }.count { it }
            val twoWins = iterationsPerChange - oneWins
            if (oneWins > twoWins) {
                startValue -= change
            } else {
                startValue += change
            }
            change /= 2
        }
        executor.shutdown()
        return startValue
    }
}