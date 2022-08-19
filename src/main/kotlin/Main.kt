import service.AIService
import service.NetworkService
import service.RootService
import view.SopraApplication

fun main() {
    //AIService.minMaxAIGame()
    //SopraApplication().show()
    mainNetwork()
    println("Application ended. Goodbye")
}

/**
 * Function for Testing Network Features
 */
fun mainNetwork(){
    val root = RootService()
    val game = NetworkService(root)
    game.hostGame("net22c", "TestGroup06", "GROUP6FTW")
}