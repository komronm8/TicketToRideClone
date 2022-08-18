import service.ai.AIService

fun main() {
    AIService.playGame(AIService::monteCarloMove)
    println("Application ended. Goodbye")
}