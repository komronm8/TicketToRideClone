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
            val refreshable = object : Refreshable {
                var ended: Player? = null
                override fun refreshAfterEndGame(winner: Player) {
                    println("winner: ${winner.name}")
                    ended = winner
                }
            }
            root.addRefreshable(refreshable)
            val aiService = AIService(root)
            aiService.root.gameService.startNewGame(players)
            val destinationCards = aiService.root.game.currentState.players.map {
                aiService.chooseDestinationCards(it as AIPlayer)
            }
            root.gameService.chooseDestinationCard(destinationCards)
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

    fun chooseDestinationCards(player: AIPlayer): List<Int> = when (player.strategy) {
        AIPlayer.Strategy.Random -> List(5) { idx -> idx }
            .shuffled()
            .subList(0, Random.nextInt(2, 6))

        is AIPlayer.Strategy.MonteCarlo -> root.monteCarloChooseDestinationCards(player)
    }
}


