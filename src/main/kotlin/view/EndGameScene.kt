package view

import service.RootService
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label

class EndGameScene(private val rootService: RootService):
    MenuScene(1920, 1080, ImageVisual("\\EndScene\\background.png")), Refreshable{

    private val restartButton = Button(
        posX = 693, posY = 940, width = 250, height = 57,
        visual = ImageVisual("\\EndScene\\restartButton.png")
    )

    private val startButton = Button(
        posX = 943, posY = 940, width = 250, height = 57,
        visual = ImageVisual("\\EndScene\\newGameButton.png")
    )

    init {
        addComponents(restartButton, startButton)
    }
}