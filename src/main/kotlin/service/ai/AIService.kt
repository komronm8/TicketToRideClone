package service.ai

import entity.AIPlayer
import entity.Player
import service.GameService
import service.RootService
import view.Refreshable
import kotlin.random.Random

/**
 * The service responsible for executing the AI strategies
 */
class AIService(private val root: RootService) {
    companion object {
        /**
         * Runs an entire game with AIs
         */
        fun runWithAI(players: List<GameService.PlayerData>): Player {
            val root = RootService()
            root.gameService.startNewGame(players)
            val destinationCards = players.map {
                when (checkNotNull(it.aiStrategy)) {
                    is AIPlayer.Strategy.MonteCarlo -> root.monteCarloChooseDestinationCards()
                    AIPlayer.Strategy.Random -> List(5) { idx -> idx }
                        .shuffled()
                        .subList(0, Random.nextInt(2, 6))
                }
            }
            root.gameService.chooseDestinationCard(destinationCards)
            val refreshable = object : Refreshable {
                var ended: Player? = null
                override fun refreshAfterEndGame(winner: Player) {
                    println("winner: ${winner.name}")
                    ended = winner
                }
            }
            root.addRefreshable(refreshable)
            val aiService = AIService(root)
            while (refreshable.ended == null) {
                aiService.executePlayerMove()
            }
            return checkNotNull(refreshable.ended)
        }

        /**
         * Pits a monte-carlo AI against a random AI with the appropriate parameters
         */
        fun runAppropriate(): Player = runWithAI(
            listOf(
                GameService.PlayerData("monty", false, AIPlayer.Strategy.MonteCarlo(100)),
                GameService.PlayerData("randy", false, AIPlayer.Strategy.Random),
            )
        )
    }

    /**
     * executes the move of an AI player
     * @throws ClassCastException if the current player is not a [AIPlayer]
     */
    fun executePlayerMove() {
        val player = root.game.currentState.currentPlayer
        player as AIPlayer
        when (player.strategy) {
            AIPlayer.Strategy.Random -> root.randomNextTurn()
            is AIPlayer.Strategy.MonteCarlo -> root.monteCarloMove(player.strategy.c, player.strategy.timeLimit)
        }
    }
}


