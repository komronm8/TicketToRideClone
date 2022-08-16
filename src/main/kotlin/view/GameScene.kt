package view

import entity.Player
import service.GameService
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
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
        posX = 1636, posY = 537, width = 100, font = Font(size = 20, fontWeight = Font.FontWeight.BOLD),
        text = root.game.currentState.currentPlayer.trainCarsAmount.toString()
    )

    private val currentPlayerImage: Label = Label(
        posX = 1720, posY = 425, width = 180, height = 180,
        visual = ImageVisual(currentPlayerFolder() + "player_profile.png")
    )

    private val currentPlayerPoints: Label = Label(
        posX = 1720, posY = 530, width = 180, font = Font(size = 28, fontWeight = Font.FontWeight.BOLD),
        text = root.game.currentState.currentPlayer.points.toString()
    )

    private val redo: Button = Button(
        width = 68, height = 83, posY = 495, posX = 1550, visual = ImageVisual("GameScene/redo.png")
    ).apply {
        isDisabled = true
        opacity = 0.5

        onMouseClicked = {
            root.redo();

            undo.isDisabled = false
            undo.opacity = 1.0

            if(root.game.currentStateIndex == root.game.states.size - 1) {
                isDisabled = true
                opacity = 0.5
            }
        }
    }

    private val undo: Button = Button(
        width = 68, height = 83, posY = 495, posX = 1465, visual = ImageVisual("GameScene/undo.png")
    ).apply {
        isDisabled = true
        opacity = 0.5

        onMouseClicked = {
            root.undo();

            redo.isDisabled = false
            redo.opacity = 1.0

            if(root.game.currentStateIndex == 0) {
                isDisabled = true
                opacity = 0.5
            }
        }
    }

    private val showCurrentPlayerCards: Button = Button(
        width = 645, height = 75, posY = 502, posX = 750, visual = ImageVisual("wood_btn.jpg"),
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE), text = "Show your cards"
    ).apply { showCards(root.game.currentState.currentPlayer) }

    init {
        opacity = 1.0
        background = ImageVisual("GameScene/background.png")

        playerBanner.addAll(trainCarLabel, currentPlayerImage, currentPlayerPoints, redo, undo, showCurrentPlayerCards)

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

    private fun showCards(playerToExpose: Player): Unit {

    }
}