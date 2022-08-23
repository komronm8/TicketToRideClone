package view

import entity.Player
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.WindowMode

/**
 * Implementation of the BGW [BoardGameApplication] for the board game"Ticket to Ride".
 */
class SopraApplication : BoardGameApplication("Zug um Zug", windowMode = WindowMode.FULLSCREEN), Refreshable{

    private val rootService: RootService = RootService()

    private var gameScene: GameScene = GameScene(rootService)

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
        this.showMenuScene(mainMenuScene)

        rootService.addRefreshables(
            this,
            mainMenuScene,
            gameScene,
            endScene,
            configScene
        )
    }

    private fun goBack(){
        this.showMenuScene(mainMenuScene)
    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
        this.showGameScene(gameScene)
    }

    override fun refreshAfterEndGame(winner: Player) {
        this.showMenuScene(endScene)
    }

}