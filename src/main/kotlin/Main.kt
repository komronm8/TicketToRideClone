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
    /*val file = Path.of("City_Enum_Zuordnung_1.csv").toFile()
    val mapping = file.readLines()
        .map { it.split(",") }
        .map { it[0] to it[1] }
    val identToName = mapping.joinToString("\n") { "\"${it.first}\"->\"${it.second}\"" }
    println(identToName)
    println("------")
    val nameToIdent = mapping.joinToString(separator = "\n") { "\"${it.second}\"->\"${it.first}\"" }
    println(nameToIdent)*/

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