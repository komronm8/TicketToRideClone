package view

import entity.Player
import service.GameService
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.WindowMode

class SopraApplication : BoardGameApplication("Zug um Zug", windowMode = WindowMode.FULLSCREEN), Refreshable{

    private var gameScene: GameScene

    private val rootService: RootService = RootService()

    private var configScene = ConfigPlayerScene(rootService).apply {
        goBackButton.onMouseClicked = {
            if(backCount == 0){
                goBack()
            }
            this.remove(backCount)
        }
    }

    private var endScene = EndGameScene(rootService).apply {
        exitButton.onMouseClicked = {
            exit()
        }
        startButton.onMouseClicked = {
            this@SopraApplication.showMenuScene(mainMenuScene)
        }
    }

    private var mainMenuScene = NewGameScene(rootService).apply {
        soloButton.onMouseClicked = {
            configScene.addSoloComponents()
            this@SopraApplication.hideMenuScene()
            this@SopraApplication.showMenuScene(configScene)
        }

        onlineButton.onMouseClicked = {
            configScene.addOnlineComponents()
            this@SopraApplication.hideMenuScene()
            this@SopraApplication.showMenuScene(configScene)
        }

        closeButton.onMouseClicked = {
            exit()
        }
    }

    init {
        rootService.gameService.startNewGame(listOf(
            GameService.PlayerData("a", false),
            GameService.PlayerData("b", false),
            GameService.PlayerData("c", false)))
        gameScene = GameScene(rootService)
        this.showGameScene(gameScene)

        this.showMenuScene(mainMenuScene)
    }

    private fun goBack(){
        this.showMenuScene(mainMenuScene)
    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
    }

    override fun refreshAfterEndGame(winner: Player) {
        this.showMenuScene(endScene)
    }

}