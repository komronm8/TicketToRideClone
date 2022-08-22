package entity.service.ai

import entity.AIPlayer
import service.GameService
import service.ai.AIService
import kotlin.test.Test

/**
 * Tests the random AI
 */
class TestRandomAI {
    /**
     * Runs a game with random AIs
     */
    @Test
    fun runNoException() {
        repeat(10) {
            AIService.runWithAI(
                listOf(
                    GameService.PlayerData("randy", false, AIPlayer.Strategy.Random),
                    GameService.PlayerData("dandy", false, AIPlayer.Strategy.Random)
                )
            )
        }
    }
}