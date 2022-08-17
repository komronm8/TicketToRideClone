package view

import service.*
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.components.uicomponents.Button

class NewGameScene(private val rootService: RootService):
    MenuScene(1920, 1080, ImageVisual("\\MenuScene\\MainMenuScreen.png")), Refreshable{

    val soloButton = Button(
        posX = 810, posY = 420, width = 315, height = 138, visual = ImageVisual("\\MenuScene\\soloButton.png")
    )

    val onlineButton = Button(
        posX = 810, posY = 550, width = 315, height = 138, visual = ImageVisual("\\MenuScene\\onlineButton.png")
    )

    val closeButton = Button(
        posX = 1770, posY = 10, width = 141, height = 143, visual = ImageVisual("\\MenuScene\\closeButton.png")
    )

    init{
        addComponents(soloButton,
        onlineButton,
        closeButton)
    }

}