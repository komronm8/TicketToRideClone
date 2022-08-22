package entity.service.ai

import entity.AIPlayer
import service.GameService
import service.ai.AIService
import kotlin.test.Test

/**
 * Tests the monte carlo AI
 */
class TestMonteCarloAI {
    /**
     * Plays one game with the monte carlo AI
     */
    @Test
    fun testMonteCarloGameNoExceptions() {
        AIService.runWithAI(
            listOf(
                GameService.PlayerData("monty", false, AIPlayer.Strategy.MonteCarlo(100)),
                GameService.PlayerData("randy", false, AIPlayer.Strategy.Random)
            )
        )
    }
}