package view

import entity.Player
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

/**
 * [MenuScene] that is displayed when the game is finished. It shows the final result of the game
 * as well as the winner. Also, there are three buttons: one for starting a new game with the same players,
 * one to go to the start page of the game and one for quitting the program.
 */
class EndGameScene(private val rootService: RootService):
    MenuScene(1920, 1080, ImageVisual("EndScene/background.png")), Refreshable{

    private val restartButton = Button(
        posX = 693, posY = 940, width = 250, height = 57,
        visual = ImageVisual("EndScene/restartButton.png")
    ).apply {
        onMouseClicked = {
            rootService.gameService.nextGame()
        }
    }

    val startButton = Button(
        posX = 943, posY = 940, width = 250, height = 57,
        visual = ImageVisual("EndScene/newGameButton.png")
    )

    val exitButton = Button(
        posX = 1790, posY = 5, width = 142, height = 113,
        visual = ImageVisual("EndScene/button-quit.png")
    )

    //PLayer icons
    private val firstPlaceIcon = Label(
        posX = 887, posY = 432, width = 100, height = 131)

    private val secondPlaceIcon = Label(
        posX = 735, posY = 490, width = 100, height = 131)

    private val thirdPlaceIcon = Label(
        posX = 1035, posY = 510, width = 100, height = 131)

    //first place labels
    private val firstPlaceLabel = Label(
        posX = 862, posY = 590, width = 150, height = 30, text = "Player1", alignment = Alignment.CENTER,
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    private val firstPlacePointsLabel = Label(
        posX = 887, posY = 610, width = 100, height = 30, text = "50 Points",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    //second place labels
    private val secondPlaceLabel = Label(
        posX = 712, posY = 620, width = 150, height = 30, text = "Player2", alignment = Alignment.CENTER,
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    private val secondPlacePointsLabel = Label(
        posX = 737, posY = 640, width = 100, height = 30, text = "40 Points",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    //third place labels
    private val thirdPlaceLabel = Label(
        posX = 1010, posY = 640, width = 150, height = 30, text = "Player3", alignment = Alignment.CENTER,
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    private val thirdPlacePointsLabel = Label(
        posX = 1035, posY = 660, width = 100, height = 30, text = "30 Points",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    //Configure the scoreboard
    private val listOfPoints = mutableListOf<Int>()
    private val players = mutableListOf<Player>()
    private fun configScoreboard(){
        val game = rootService.game.currentState
        for( i in game.players.sortedByDescending { it.points } ){
            players.add(i)
            listOfPoints.add(i.points)
        }
        if(game.players.size == 2) configFor2Players() else configFor3Player()
    }

    private fun configFor2Players(){
        //if the first place points are from player1
        if(listOfPoints[0] == players[0].points){
            firstPlaceLabel.text = players[0].name
            firstPlacePointsLabel.text = "${listOfPoints[0]} Points"
            changeIcon(players[0], firstPlaceIcon)
            secondPlaceLabel.text = players[1].name
            secondPlacePointsLabel.text = "${listOfPoints[1]} Points"
            changeIcon(players[1], secondPlaceIcon)
        }
        //else the first place points are from player2
        else{
            firstPlaceLabel.text = players[1].name
            firstPlacePointsLabel.text = "${listOfPoints[1]} Points"
            changeIcon(players[1], firstPlaceIcon)
            secondPlaceLabel.text = players[0].name
            secondPlacePointsLabel.text = "${listOfPoints[0]} Points"
            changeIcon(players[0], secondPlaceIcon)
        }
        addComponents(firstPlaceLabel, firstPlacePointsLabel, secondPlaceLabel, secondPlacePointsLabel,
            firstPlaceIcon, secondPlaceIcon)
    }

    private fun configFor3Player(){
        for ( i in players ){
            when(i.points){
                listOfPoints[0] -> {
                    firstPlaceLabel.text = i.name
                    firstPlacePointsLabel.text = "${listOfPoints[0]} Points"
                    changeIcon(i, firstPlaceIcon)
                }
                listOfPoints[1] -> {
                    secondPlaceLabel.text = i.name
                    secondPlacePointsLabel.text = "${listOfPoints[1]} Points"
                    changeIcon(i, secondPlaceIcon)
                }
                else -> {
                    thirdPlaceLabel.text = i.name
                    thirdPlacePointsLabel.text = "${listOfPoints[2]} Points"
                    changeIcon(i, thirdPlaceIcon)
                }
            }
        }
        addComponents(firstPlaceLabel, firstPlacePointsLabel, secondPlaceLabel, secondPlacePointsLabel,
        thirdPlaceLabel, thirdPlacePointsLabel, firstPlaceIcon, secondPlaceIcon, thirdPlaceIcon)
    }

    private fun changeIcon(player: Player, icon: Label){
        val playerIcons = arrayOf("yellowPlayerIcon.png", "purplePlayerIcon.png", "redPlayerIcon.png")
        for( i in 0 until players.size){
            if( players[i] == player ){
                icon.visual = ImageVisual("EndScene/" + playerIcons[i])
                print(i)
            }
        }
    }

    override fun refreshAfterEndGame(winner: Player) {
        BoardGameApplication.runOnGUIThread {
            clearComponents()
            addComponents(restartButton, startButton, exitButton)
            configScoreboard()
        }
    }

}