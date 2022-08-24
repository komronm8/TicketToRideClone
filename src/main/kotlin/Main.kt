import entity.AIPlayer
import service.GameService
import service.NetworkService
import service.RootService
import service.ai.AIService


fun main() {
    //AIService.minMaxAIGame()
    //SopraApplication().show()
    //mainNetwork()
    AIService.checkRun(
        30, listOf(
            GameService.PlayerData("monty", false, AIPlayer.Strategy.MonteCarlo(2000)),
            GameService.PlayerData("randy", false, AIPlayer.Strategy.Random),
        )
    )
    //println("Application ended. Goodbye")
}

/**
 * Function for Testing Network Features
 */
fun mainNetwork() {
    val root = RootService()
    val game = NetworkService(root)
    //game.hostGame("net22c", "TestGroupHOST", "TEST2223")
    game.joinGame("net22c", "Johannes", "5612")
}