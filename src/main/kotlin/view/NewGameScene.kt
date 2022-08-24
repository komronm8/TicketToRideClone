package view

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.components.uicomponents.Button

/**
 * [MenuScene] that is used for choosing which mode to play, solo or online.
 * By pressing [soloButton] or [onlineButton] the configPlayerScene will be shown.
 * This menu is displayed directly at program start.
 * There is also a [closeButton]  to end the program.
 */
class NewGameScene(private val rootService: RootService):
    MenuScene(1920, 1080, ImageVisual("MenuScene/MainMenuScreen.png")), Refreshable{

    val soloButton = Button(
        posX = 810, posY = 420, width = 315, height = 138, visual = ImageVisual("MenuScene/soloButton.png")
    )

    val onlineButton = Button(
        posX = 810, posY = 550, width = 315, height = 138, visual = ImageVisual("MenuScene/onlineButton.png")
    )

    val closeButton = Button(
        posX = 1770, posY = 10, width = 141, height = 143, visual = ImageVisual("MenuScene/closeButton.png")
    )

    init{
        addComponents(soloButton,
        onlineButton,
        closeButton)
    }

}