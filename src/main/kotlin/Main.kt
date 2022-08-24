import service.network.NetworkService
import service.RootService
import view.SopraApplication

/**
 * Starting point for App
 */
fun main() {
    //AIService.minMaxAIGame()
    SopraApplication().show()
    //mainNetwork()

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