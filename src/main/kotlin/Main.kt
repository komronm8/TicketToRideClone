import service.NetworkService
import service.RootService
import service.ai.AIService
import view.SopraApplication


fun main() {
    //AIService.minMaxAIGame()
    //SopraApplication().show()
    //mainNetwork()
    repeat(10) { AIService.runAppropriate() }
    println("Application ended. Goodbye")
}

/**
 * Function for Testing Network Features
 */
fun mainNetwork(){
    val root = RootService()
    val game = NetworkService(root)
    //game.hostGame("net22c", "TestGroupHOST", "TEST2223")
    game.joinGame("net22c", "Johannes", "5612")
}