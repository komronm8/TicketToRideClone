package view

import service.GameService
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

class SopraApplication : BoardGameApplication("Zug um Zug"), Refreshable {
    private val rootService: RootService = RootService()

    private var gameScene: GameScene

    init {
        rootService.gameService.startNewGame(listOf(
            GameService.PlayerData("a", false),
            GameService.PlayerData("b", false),
            GameService.PlayerData("c", false)))
        gameScene = GameScene(rootService)
        this.showGameScene(gameScene)
    }
}