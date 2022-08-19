package view

import service.*
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.core.Alignment
import java.awt.Color

class ConfigPlayerScene(private val rootService: RootService):
    MenuScene(1920, 1080, ImageVisual("\\ConfigScene\\configBackground.png")), Refreshable{

    private var playerCount = 2
    private var player1Type = arrayOf("Human", "RandomAI", "UnfairAI")
    private var player2Type = player1Type
    private var player3Type = player1Type
    private var onlinePlayerType = player1Type
    var backCount = 0

    //Solo menu
    //Player1
    private val player1Label = Label(
        posX = 800, posY = 350, width = 177, height = 55, text = "Player1",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 28),
        alignment = Alignment.TOP_CENTER, visual = ImageVisual("\\ConfigScene\\yellowPlayerLabel.png")
    )

    private val player1Input = TextField(
        posX = 1000, posY = 350, width = 250, height = 20, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val bot1Button = Button(
        posX = 1275, posY = 350, width = 45, height = 45,
        visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
    ).apply {
        onMouseClicked = {
            changeTypeButton(this, player1TypeLabel, player1Type)
        }
    }

    private val player1TypeLabel = Label(
        posX = 1025, posY = 390, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("\\ConfigScene\\smallWoodBg.png")
    )

    private val player1Icon = Button(
        posX = 1370, posY = 300, width = 81, height = 117,
        visual = ImageVisual("\\ConfigScene\\yellowPlayerIcon.png")
    )

    //Player2
    private val player2Label = Label(
        posX = 800, posY = 475, width = 177, height = 55, text = "Player2",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 28),
        alignment = Alignment.TOP_CENTER, visual = ImageVisual("\\ConfigScene\\purplePlayerLabel.png")
    )

    private val player2Input = TextField(
        posX = 1000, posY = 475, width = 250, height = 20, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val bot2Button = Button(
        posX = 1275, posY = 475, width = 45, height = 45,
        visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
    ).apply {
        onMouseClicked = {
            changeTypeButton(this, player2TypeLabel, player2Type)
        }
    }

    private val player2TypeLabel = Label(
        posX = 1025, posY = 515, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("\\ConfigScene\\smallWoodBg.png")
    )

    private val player2Icon = Button(
        posX = 1365, posY = 425, width = 96, height = 128,
        visual = ImageVisual("\\ConfigScene\\purplePlayerIcon.png")
    )

    //Player3
    private val player3Label = Label(
        posX = 800, posY = 600, width = 177, height = 55, text = "Player3",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 28),
        alignment = Alignment.TOP_CENTER, visual = ImageVisual("\\ConfigScene\\redPlayerLabel.png")
    )

    private val player3Input = TextField(
        posX = 1000, posY = 600, width = 250, height = 20, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val bot3Button = Button(
        posX = 1275, posY = 600, width = 45, height = 45,
        visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
    ).apply{
        onMouseClicked = {
            changeTypeButton(this, player3TypeLabel, player3Type)
        }
    }

    private val player3TypeLabel = Label(
        posX = 1025, posY = 640, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("\\ConfigScene\\smallWoodBg.png")
    )

    private val player3Icon = Button(
        posX = 1375, posY = 550, width = 80, height = 130,
        visual = ImageVisual("\\ConfigScene\\redPlayerIcon.png")
    )

    private val playerInputs = arrayOf(player1Input, player2Input, player3Input)
    private val playerTypeLabels = arrayOf(player1TypeLabel, player2TypeLabel, player3TypeLabel)
    private val botButtons = arrayOf(bot1Button, bot2Button, bot3Button)

    private val startButton = Button(
        posX = 850, posY = 720, width = 401, height = 83,
        visual = ImageVisual("\\ConfigScene\\startGameButton.png")
    ).apply {
        onMouseClicked = {
            val playerList = mutableListOf<GameService.PlayerData>()
            var checked = false
            for(i in 0 until playerCount){
                if(playerInputs[i].text != ""){
                    playerList.add( GameService.PlayerData(playerInputs[i].text, false) )
                    checked = true
                }
                else{
                    checked = false
                    break
                }
            }
            if(checked){ rootService.gameService.startNewGame(playerList) }
        }
    }

    private val removeButton = Button(
        posX = 1350, posY = 735, width = 66, height = 67,
        visual = ImageVisual("\\ConfigScene\\removeButton.png")
    ).apply {
        onMouseClicked = {
            if(playerCount == 3){
                removeComponents(player3Label, player3Input, player3Icon, bot3Button, player3TypeLabel)
                player3Input.text = ""
                playerCount--
            }
        }
    }

    private val addButton = Button(
        posX = 1420, posY = 745, width = 45, height = 46,
        visual = ImageVisual("\\ConfigScene\\addButton.png")
    ).apply {
        onMouseClicked = {
            if(playerCount == 2) {
                addComponents(player3Label, player3Input, player3Icon, bot3Button, player3TypeLabel)
                playerCount++
            }
        }
    }

    private val garrySoloIcon = Label(
        posX = 95, posY = 175, width = 600, height = 911,
        visual = ImageVisual("\\ConfigScene\\GarrySolo.png")
    )

    val goBackButton = Button(
        posX = 5, posY = 5, width = 137, height = 135,
        visual = ImageVisual("\\ConfigScene\\backButton.png")
    )

    //Online menu
    private val joinButton = Button(
        posX = 900, posY = 450, width = 283.53, height = 170,
        visual = ImageVisual("\\ConfigScene\\joinButton.png")
    ).apply {
        onMouseClicked = {
            backCount++
            removeOnlineComponents()
            addComponents(sessionLabel, sessionTextField, joinSessionButton, playerNameLabel,
                playerNameInput, onlinePlayerTypeButton, onlinePlayerTypeLabel)
        }
    }

    private val hostButton = Button(
        posX = 1180, posY = 450, width = 212.5, height = 170,
        visual = ImageVisual("\\ConfigScene\\createButton.png")
    ).apply {
        onMouseClicked = {
            backCount++
            removeOnlineComponents()
            addComponents(sessionLabel, sessionTextField, hostSessionButton, playerNameLabel,
                playerNameInput, onlinePlayerTypeButton, onlinePlayerTypeLabel, hostRandomSessionIDButton)
        }
    }

    private val garryOnlineIcon = Label(
        posX = 95, posY = 175, width = 600, height = 911,
        visual = ImageVisual("\\ConfigScene\\GarryOnline.png")
    )

    //Join/Host components
    private val sessionLabel = Label(
        posX = 930, posY = 350, width = 418, height = 85,
        visual = ImageVisual("\\ConfigScene\\textBg.png")
    )

    private val sessionTextField = TextField(
        posX = 1050, posY = 370, width = 179, height = 40, prompt = "<Session ID>",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24),
    ).apply {
        opacity = 0.5
    }

    private val joinSessionButton = Button(
        posX = 1015, posY = 650, width = 250, height = 84,
        visual = ImageVisual("\\ConfigScene\\joinSessionButton.png")
    )

    private val playerNameLabel = Label(
        posX = 840, posY = 450, width = 600, height = 119,
        visual = ImageVisual("\\ConfigScene\\playerNameLabel.png")
    )

    private val playerNameInput = TextField(
        posX = 900, posY = 495, width = 400, height = 50, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 26)
    )

    private val onlinePlayerTypeButton = Button(
        posX = 1325, posY = 495, width = 45, height = 45,
        visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
    ).apply {
        onMouseClicked = {
            changeTypeButton(this, onlinePlayerTypeLabel, onlinePlayerType)
        }
    }

    private val onlinePlayerTypeLabel = Label(
        posX = 1040, posY = 550, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("\\ConfigScene\\smallWoodBg.png")
    )

    private val hostSessionButton = Button(
        posX = 1015, posY = 650, width = 250, height = 84,
        visual = ImageVisual("\\ConfigScene\\hostSessionButton.png")
    )

    private val hostRandomSessionIDButton = Button(
        posX = 1320, posY = 352, width = 100, height = 83,
        visual = ImageVisual("\\ConfigScene\\randomButton.png")
    )

    //Methods
    fun addSoloComponents(){
        addComponents(
            player1Label, player2Label,
            player1Input, player2Input,
            bot1Button, player1TypeLabel, player1Icon,
            bot2Button, player2TypeLabel, player2Icon,
            startButton, removeButton, addButton, goBackButton, garrySoloIcon)
    }

    fun addOnlineComponents(){
        addComponents(
            joinButton, hostButton, goBackButton, garryOnlineIcon
        )
    }

    fun remove(i: Int){
        when(i){
            0 -> {
                playerCount = 2
                for(x in 0 until 3){
                    playerInputs[x].text = ""
                    playerTypeLabels[x].text = "Human"
                    botButtons[x].visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
                }
                bot3Button.apply {
                }
                clearComponents()
            }
            1 -> {
                removeComponents(sessionLabel, sessionTextField, hostSessionButton, playerNameLabel, playerNameInput,
                    onlinePlayerTypeButton, onlinePlayerTypeLabel, hostRandomSessionIDButton, joinSessionButton)
                addComponents(joinButton, hostButton)
                sessionTextField.text = ""
                playerNameInput.text = ""
                onlinePlayerTypeButton.visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
                onlinePlayerTypeLabel.text = "Human"
                backCount--
            }
        }
    }

    private fun removeOnlineComponents(){
        removeComponents(joinButton, hostButton)
    }

    private fun changeTypeButton( button: Button, label: Label, typeArray: Array<String> ){
        when(label.text){
            "Human" -> {
                button.visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
                label.text = typeArray[1]
            }
            "RandomAI" -> {
                button.visual = ImageVisual("\\ConfigScene\\changeToHumanButton.png")
                label.text = typeArray[2]
            }
            "UnfairAI" -> {
                button.visual = ImageVisual("\\ConfigScene\\changeToAIButton.png")
                label.text = typeArray[0]
            }
        }
    }
}