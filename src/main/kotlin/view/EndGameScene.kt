package view

import entity.Player
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color


class EndGameScene(private val rootService: RootService):
    MenuScene(1920, 1080, ImageVisual("\\EndScene\\background.png")), Refreshable{

    private val restartButton = Button(
        posX = 693, posY = 940, width = 250, height = 57,
        visual = ImageVisual("\\EndScene\\restartButton.png")
    ).apply {
        onMouseClicked = {
            rootService.gameService.nextGame()
        }
    }

    val startButton = Button(
        posX = 943, posY = 940, width = 250, height = 57,
        visual = ImageVisual("\\EndScene\\newGameButton.png")
    )

    val exitButton = Button(
        posX = 1790, posY = 5, width = 142, height = 113,
        visual = ImageVisual("\\EndScene\\button-quit.png")
    )

    //PLayer icons
    private val playerYellowIcon = Label(
        posX = 750, posY = 500, width = 81, height = 117,
        visual = ImageVisual("\\EndScene\\yellowPlayerIcon.png")
    )

    private val playerRedIcon = Label(
        posX = 897, posY = 430, width = 80, height = 130,
        visual = ImageVisual("\\EndScene\\redPlayerIcon.png")
    )

    private val playerPurpleIcon = Label(
        posX = 1035, posY = 520, width = 96, height = 128,
        visual = ImageVisual("\\EndScene\\purplePlayerIcon.png")
    )

    //first place labels
    private val firstPlaceLabel = Label(
        posX = 860, posY = 590, width = 150, height = 30, text = "Player1", alignment = Alignment.CENTER,
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    private val firstPlacePointsLabel = Label(
        posX = 885, posY = 610, width = 100, height = 30, text = "50 Points",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    //second place labels
    private val secondPlaceLabel = Label(
        posX = 715, posY = 620, width = 150, height = 30, text = "Player2", alignment = Alignment.CENTER,
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 20)
    )

    private val secondPlacePointsLabel = Label(
        posX = 740, posY = 640, width = 100, height = 30, text = "40 Points",
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
        for( i in game.players ){
            players.add(i)
            listOfPoints.add(i.points)
        }
        listOfPoints.sortDescending()
        if(game.players.size == 2) configFor2Players() else configFor3Player()
    }

    //TODO Configure player icons for scoreboard

    private fun configFor2Players(){
        //if the first place points are from player1
        if(listOfPoints[0] == players[0].points){
            firstPlaceLabel.text = players[0].name
            firstPlacePointsLabel.text = "${listOfPoints[0]} Points"
            secondPlaceLabel.text = players[1].name
            secondPlacePointsLabel.text = "${listOfPoints[1]} Points"
        }
        //else the first place points are from player2
        else{
            firstPlaceLabel.text = players[1].name
            firstPlacePointsLabel.text = "${listOfPoints[1]} Points"
            secondPlaceLabel.text = players[0].name
            secondPlacePointsLabel.text = "${listOfPoints[0]} Points"
        }
        addComponents(firstPlaceLabel, firstPlacePointsLabel, secondPlaceLabel, secondPlacePointsLabel)
    }

    private fun configFor3Player(){
        for ( i in players ){
            when(i.points){
                listOfPoints[0] -> {
                    firstPlaceLabel.text = i.name
                    firstPlacePointsLabel.text = "${listOfPoints[0]} Points"
                }
                listOfPoints[1] -> {
                    secondPlaceLabel.text = i.name
                    secondPlacePointsLabel.text = "${listOfPoints[1]} Points"
                }
                else -> {
                    thirdPlaceLabel.text = i.name
                    thirdPlacePointsLabel.text = "${listOfPoints[2]} Points"
                }
            }
        }
        addComponents(firstPlaceLabel, firstPlacePointsLabel, secondPlaceLabel, secondPlacePointsLabel,
        thirdPlaceLabel, thirdPlacePointsLabel)
    }

    override fun refreshAfterEndGame(winner: Player) {
        clearComponents()
        addComponents(restartButton, startButton, exitButton, playerYellowIcon, playerRedIcon, playerPurpleIcon)
        configScoreboard()
    }


}