package view

import service.GameService
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
//import tools.aqua.bgw.core.WindowMode(for fullscreen)

class SopraApplication : BoardGameApplication("Zug um Zug"), Refreshable {

    //If wanted, we can make window fullscreen, by adding "windowMode = WindowMode.FULLSCREEN"

    private var gameScene: GameScene

    private val rootService: RootService = RootService()

    private var configScene = ConfigPlayerScene(rootService).apply {
        goBackButton.onMouseClicked = {
            goBack()
            this.remove()
        }
    }

    private var endScene = EndGameScene(rootService)

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

//        this.showMenuScene(mainMenuScene)
    }

    private fun goBack(){
        this.showMenuScene(mainMenuScene)
    }

}