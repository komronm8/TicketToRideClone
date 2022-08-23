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
            val destinationCards = aiService.root.game.currentState.players.associate {
                it.name to aiService.chooseDestinationCards(it as AIPlayer)
            }
            root.gameService.chooseDestinationCard(destinationCards)
            while (refreshable.ended == null) {
                aiService.executePlayerMove { it() }
            }
            return checkNotNull(refreshable.ended)
        }

        /**
         * Pits a monte-carlo AI against a random AI with the appropriate parameters
         */
        fun runAppropriate(): Player = runWithAI(
            listOf(
                GameService.PlayerData("monty", false, AIPlayer.Strategy.MonteCarlo(1000)),
                GameService.PlayerData("randy", false, AIPlayer.Strategy.Random),
            )
        )

        fun checkRun(times: Int, players: List<GameService.PlayerData>) {
            val winners = HashMap<String, Int>(players.size)
            players.forEach { winners[it.name] = 0 }
            repeat(times) {
                val winner = runWithAI(players)
                winners.computeIfPresent(winner.name) { _, wins -> wins + 1 }
                val runs = (it + 1).toDouble()
                println(winners.map { "${it.key}: ${it.value.toDouble() / runs}" }.joinToString(separator = ", "))
            }
            println(winners)
        }
    }

    /**
     * executes the move of an AI player
     * @throws ClassCastException if the current player is not a [AIPlayer]
     */
    fun executePlayerMove(execute: (()->Unit)->Unit) {
        val player = root.game.currentState.currentPlayer
        player as AIPlayer
        when (player.strategy) {
            AIPlayer.Strategy.Random -> execute { root.randomNextTurn() }
            is AIPlayer.Strategy.MonteCarlo -> root.monteCarloMove(player.strategy.c, player.strategy.timeLimit, execute)
        }
    }

    fun chooseDestinationCards(player: AIPlayer): List<Int> = when (player.strategy) {
        AIPlayer.Strategy.Random -> List(5) { idx -> idx }
            .shuffled()
            .subList(0, Random.nextInt(2, 6))

        is AIPlayer.Strategy.MonteCarlo -> root.monteCarloChooseDestinationCards(player)
    }
}


