package view

import service.GameService
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

class GameScene(private val root: RootService) : BoardGameScene(1920, 1080), Refreshable {
    private val playerBanner: Pane<UIComponent> = Pane(
        0, 480, 1920, 600, ImageVisual(currentPlayerFolder() + "dock.png")
    )

    private val trainCarLabel: Label = Label(
        posX = 1000, posY = 500, width = 100, height = 100, alignment = Alignment.CENTER,
        text = /*root.game.currentState.currentPlayer.trainCarsAmount.toString()*/ "XX", font = Font(size = 20)
    )

    init {
        opacity = 1.0
        background = ImageVisual("GameScene/background.png")

        playerBanner.add(trainCarLabel)

        addComponents(
            playerBanner
        )
    }

    private fun currentPlayerFolder(): String {
        return when(root.game.currentState.currentPlayerIndex) {
            0 -> "GameScene/Player/Purple/"
            1 -> "GameScene/Player/Yellow/"
            else -> "GameScene/Player/Red/"
        }
    }
}