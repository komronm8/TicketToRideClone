import service.ai.AIService

fun main() {
    var montyCount = 0
    repeat(10) {
        val gc = it + 1
        val winner = AIService.runAppropriate()
        if (winner.name =="monty") montyCount += 1
        println("games: $gc, wins: $montyCount, wr: ${montyCount.toDouble() / gc.toDouble()}, winner: ${winner.name}")
    }
    //println(MonteCarloOptimizer.optimizeC(35))
    println("Application ended. Goodbye")
}