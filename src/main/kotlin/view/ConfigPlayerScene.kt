package view

import service.*
import entity.AIPlayer
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.core.Alignment
import java.awt.Color

/**
 * The menuScene for the two modes. After choosing which mode to play, online or solo, this scene will pop up
 * for the individual modes. For solo mode, the amount of players as well as their names and types can be configured.
 * For online mode, the options available are joining a game with a sessionID, or creating one yourself by hosting it
 */
class ConfigPlayerScene(private val rootService: RootService):
    MenuScene(1920, 1080, ImageVisual("ConfigScene/configBackground.png")), Refreshable{

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
        alignment = Alignment.TOP_CENTER, visual = ImageVisual("ConfigScene/yellowPlayerLabel.png")
    )

    private val player1Input = TextField(
        posX = 1000, posY = 350, width = 250, height = 20, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val bot1Button = Button(
        posX = 1275, posY = 350, width = 45, height = 45,
        visual = ImageVisual("ConfigScene/changeToAIButton.png")
    ).apply {
        onMouseClicked = {
            changeTypeButton(this, player1TypeLabel, player1Type)
        }
    }

    private val player1TypeLabel = Label(
        posX = 1025, posY = 390, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("ConfigScene/smallWoodBg.png")
    )

    private val player1Icon = Button(
        posX = 1370, posY = 300, width = 81, height = 117,
        visual = ImageVisual("ConfigScene/yellowPlayerIcon.png")
    )

    //Player2
    private val player2Label = Label(
        posX = 800, posY = 475, width = 177, height = 55, text = "Player2",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 28),
        alignment = Alignment.TOP_CENTER, visual = ImageVisual("ConfigScene/purplePlayerLabel.png")
    )

    private val player2Input = TextField(
        posX = 1000, posY = 475, width = 250, height = 20, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val bot2Button = Button(
        posX = 1275, posY = 475, width = 45, height = 45,
        visual = ImageVisual("ConfigScene/changeToAIButton.png")
    ).apply {
        onMouseClicked = {
            changeTypeButton(this, player2TypeLabel, player2Type)
        }
    }

    private val player2TypeLabel = Label(
        posX = 1025, posY = 515, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("ConfigScene/smallWoodBg.png")
    )

    private val player2Icon = Button(
        posX = 1365, posY = 425, width = 96, height = 128,
        visual = ImageVisual("ConfigScene/purplePlayerIcon.png")
    )

    //Player3
    private val player3Label = Label(
        posX = 800, posY = 600, width = 177, height = 55, text = "Player3",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 28),
        alignment = Alignment.TOP_CENTER, visual = ImageVisual("ConfigScene/redPlayerLabel.png")
    )

    private val player3Input = TextField(
        posX = 1000, posY = 600, width = 250, height = 20, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val bot3Button = Button(
        posX = 1275, posY = 600, width = 45, height = 45,
        visual = ImageVisual("ConfigScene/changeToAIButton.png")
    ).apply{
        onMouseClicked = {
            changeTypeButton(this, player3TypeLabel, player3Type)
        }
    }

    private val player3TypeLabel = Label(
        posX = 1025, posY = 640, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("ConfigScene/smallWoodBg.png")
    )

    private val player3Icon = Button(
        posX = 1375, posY = 550, width = 80, height = 130,
        visual = ImageVisual("ConfigScene/redPlayerIcon.png")
    )

    private val playerInputs = arrayOf(player1Input, player2Input, player3Input)
    private val playerTypeLabels = arrayOf(player1TypeLabel, player2TypeLabel, player3TypeLabel)
    private val botButtons = arrayOf(bot1Button, bot2Button, bot3Button)

    private val startButton = Button(
        posX = 850, posY = 720, width = 401, height = 83,
        visual = ImageVisual("ConfigScene/startGameButton.png")
    ).apply {
        onMouseClicked = {
            val playerList = mutableListOf<GameService.PlayerData>()
            var checked = false
            val playerTypeLabels = listOf(player1TypeLabel, player2TypeLabel, player3TypeLabel)
            for(i in 0 until playerCount){
                if(playerInputs[i].text != ""){
                    playerList.add( GameService.PlayerData(playerInputs[i].text, false,
                        getAIStrategy(playerTypeLabels[i])) )
                    checked = true
                }
                else{
                    checked = false
                    break
                }
            }
            if(checked && !checkIfSame()){ rootService.gameService.startNewGame(playerList) }
        }
    }

    private fun checkIfSame(): Boolean{
        return player1Input.text == player2Input.text || player1Input.text == player3Input.text ||
                player2Input.text == player3Input.text
    }

    private fun getAIStrategy(label: Label): AIPlayer.Strategy?{
        return when(label.text){
            "RandomAI" -> { AIPlayer.Strategy.Random }
            "UnfairAI" -> { AIPlayer.Strategy.MonteCarlo(2000) }
            else -> { null }
        }
    }

    private val removeButton = Button(
        posX = 1350, posY = 735, width = 66, height = 67,
        visual = ImageVisual("ConfigScene/removeButton.png")
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
        visual = ImageVisual("ConfigScene/addButton.png")
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
        visual = ImageVisual("ConfigScene/GarrySolo.png")
    )

    val goBackButton = Button(
        posX = 5, posY = 5, width = 137, height = 135,
        visual = ImageVisual("ConfigScene/backButton.png")
    )

    //Online menu
    private val joinButton = Button(
        posX = 900, posY = 450, width = 283.53, height = 170,
        visual = ImageVisual("ConfigScene/joinButton.png")
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
        visual = ImageVisual("ConfigScene/createButton.png")
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
        visual = ImageVisual("ConfigScene/GarryOnline.png")
    )

    //Join/Host components
    private val sessionLabel = Label(
        posX = 930, posY = 350, width = 418, height = 85,
        visual = ImageVisual("ConfigScene/textBg.png")
    )

    private val sessionTextField = TextField(
        posX = 1050, posY = 370, width = 179, height = 40, prompt = "<Session ID>",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 24),
    ).apply {
        opacity = 0.5
    }

    private var hostLobby = false

    private val joinSessionButton = Button(
        posX = 1015, posY = 650, width = 250, height = 84,
        visual = ImageVisual("ConfigScene/joinSessionButton.png")
    ).apply {
        onMouseClicked = {
            if( sessionTextField.text != "" && playerNameInput.text != "" ){
                rootService.network.joinGame("net22c", playerNameInput.text, sessionTextField.text)
                showJoinLobby()
                backCount++
            }
        }
    }

    private val playerNameLabel = Label(
        posX = 840, posY = 450, width = 600, height = 119,
        visual = ImageVisual("ConfigScene/playerNameLabel.png")
    )

    private val playerNameInput = TextField(
        posX = 900, posY = 495, width = 400, height = 50, prompt = "Player name here",
        font = Font(color = Color.BLACK, fontWeight = Font.FontWeight.BOLD, size = 26)
    )

    private val onlinePlayerTypeButton = Button(
        posX = 1325, posY = 495, width = 45, height = 45,
        visual = ImageVisual("ConfigScene/changeToAIButton.png")
    ).apply {
        onMouseClicked = {
            changeTypeButton(this, onlinePlayerTypeLabel, onlinePlayerType)
        }
    }

    private val onlinePlayerTypeLabel = Label(
        posX = 1040, posY = 550, width = 200, height = 50, text = "Human",
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24),
        visual = ImageVisual("ConfigScene/smallWoodBg.png")
    )

    private val hostSessionButton = Button(
        posX = 1015, posY = 650, width = 250, height = 84,
        visual = ImageVisual("ConfigScene/hostSessionButton.png")
    ).apply {
        onMouseClicked = {
            if( sessionTextField.text != "" && playerNameInput.text != ""  ){
                rootService.network.hostGame("net22c", playerNameInput.text, sessionTextField.text)
                hostSessionIDClipboard.text = "SID: " + sessionTextField.text
                showHostLobby()
                player1LobbyLabel.text = "Player1: " + playerNameInput.text
                addComponents(player1LobbyLabel)
                backCount++
                hostLobby = true
            }
        }
    }

    private val hostRandomSessionIDButton = Button(
        posX = 1320, posY = 352, width = 100, height = 83,
        visual = ImageVisual("ConfigScene/randomButton.png")
    ).apply {
        onMouseClicked = {
            sessionTextField.text = (1000..9999).random().toString()
        }
    }

    //OnlineSessionLobby
    private val statusLabel = Label(
        posX = 790, posY = 250, width = 700, height = 570,
        visual = ImageVisual("ConfigScene/lobbyBg.png")
    )

    private val statusLobbyLabel = Label(
        posX = 830, posY = 350, width = 600, height = 70, alignment = Alignment.CENTER,
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val hostStartButton = Button(
        posX = 1200, posY = 745, width = 200, height = 60, text = "Start Game", alignment = Alignment.CENTER,
        visual = ImageVisual("ConfigScene/startHostGameButton.png"),
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24)
    ).apply {
        onMouseClicked = {
            val client = rootService.network.client
            checkNotNull(client)
            val playerList = mutableListOf<GameService.PlayerData>()
            playerList.add(GameService.PlayerData(playerNameInput.text, false,
                getAIStrategy(onlinePlayerTypeLabel)))
            playerList.add(GameService.PlayerData(client.playersNames[1], true))
            if( client.playersNames.size  == 3  ){
                playerList.add(GameService.PlayerData(client.playersNames[2], true))
            }
            rootService.gameService.startNewGame(playerList)
        }
    }

    private val hostSessionIDClipboard = Label(
        posX = 900, posY = 745, width = 250, height = 60, alignment = Alignment.CENTER,
        visual = ImageVisual("ConfigScene/smallWoodBg.png"),
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val player1LobbyLabel = Label(
        posX = 890, posY = 440, width = 500, height = 66, alignment = Alignment.CENTER,
        visual = ImageVisual("ConfigScene/yellowPlayerLobbyIcon.png"),
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val player2LobbyLabel = Label(
        posX = 890, posY = 540, width = 500, height = 66, alignment = Alignment.CENTER,
        visual = ImageVisual("ConfigScene/purplePlayerLobbyIcon.png"),
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    private val player3LobbyLabel = Label(
        posX = 890, posY = 640, width = 500, height = 66, alignment = Alignment.CENTER,
        visual = ImageVisual("ConfigScene/redPlayerLobbyIcon.png"),
        font = Font(color = Color.WHITE, fontWeight = Font.FontWeight.BOLD, size = 24)
    )

    //host disconnect notification
    private val hostDisconnected = Label(
        posX = 400, posY = 40, width = 373, height = 187, alignment = Alignment.CENTER,
        visual = ImageVisual("ConfigScene/bubble.png")
    )

    //Methods
    /**
     * Function for adding the solo components of the config scene
     */
    fun addSoloComponents(){
        addComponents(
            player1Label, player2Label,
            player1Input, player2Input,
            bot1Button, player1TypeLabel, player1Icon,
            bot2Button, player2TypeLabel, player2Icon,
            startButton, removeButton, addButton, goBackButton, garrySoloIcon)
    }

    /**
     * Function for adding the online components of the config scene
     */
    fun addOnlineComponents(){
        addComponents(
            joinButton, hostButton, goBackButton, garryOnlineIcon
        )
    }

    /**
     * This function is used for making it possible to go back in between scenes.
     * @param sceneDepth is used to make clear which components have to be added/removed when going back
     */
    fun remove(sceneDepth: Int){
        when(sceneDepth){
            0 -> {
                playerCount = 2
                for(x in 0 until 3){
                    playerInputs[x].text = ""
                    playerTypeLabels[x].text = "Human"
                    botButtons[x].visual = ImageVisual("ConfigScene/changeToAIButton.png")
                }
                bot3Button.apply {
                }
                clearComponents()
            }
            1 -> {
                removeComponents(sessionLabel, sessionTextField, hostSessionButton, playerNameLabel, playerNameInput,
                    onlinePlayerTypeButton, onlinePlayerTypeLabel, hostRandomSessionIDButton, joinSessionButton,
                    statusLabel, statusLobbyLabel)
                addComponents(joinButton, hostButton)
                sessionTextField.text = ""
                playerNameInput.text = ""
                onlinePlayerTypeButton.visual = ImageVisual("ConfigScene/changeToAIButton.png")
                onlinePlayerTypeLabel.text = "Human"
                backCount--
            }
            2 -> {
                rootService.network.disconnect()
                removeComponents(hostStartButton, hostSessionIDClipboard,
                    player1LobbyLabel, player2LobbyLabel, player3LobbyLabel)
                backCount--
                hostLobby = false
                remove(backCount)
            }
        }
    }

    private fun removeOnlineComponents(){
        removeComponents(joinButton, hostButton, hostDisconnected)
    }

    private fun changeTypeButton( button: Button, label: Label, typeArray: Array<String> ){
        when(label.text){
            "Human" -> {
                button.visual = ImageVisual("ConfigScene/changeToAIButton.png")
                label.text = typeArray[1]
            }
            "RandomAI" -> {
                button.visual = ImageVisual("ConfigScene/changeToHumanButton.png")
                label.text = typeArray[2]
            }
            "UnfairAI" -> {
                button.visual = ImageVisual("ConfigScene/changeToAIButton.png")
                label.text = typeArray[0]
            }
        }
    }

    private fun showJoinLobby(){
        removeComponents(sessionLabel, sessionTextField, hostSessionButton, playerNameLabel, playerNameInput,
            onlinePlayerTypeButton, onlinePlayerTypeLabel, hostRandomSessionIDButton, joinSessionButton)
        statusLobbyLabel.text = "WAITING FOR HOST TO START THE GAME"
        addComponents(statusLabel, statusLobbyLabel)
    }

    private fun showHostLobby(){
        removeComponents(sessionLabel, sessionTextField, hostSessionButton, playerNameLabel, playerNameInput,
            onlinePlayerTypeButton, onlinePlayerTypeLabel, hostRandomSessionIDButton, joinSessionButton)
        statusLobbyLabel.text = "WAITING FOR PLAYERS TO CONNECT TO LOBBY"
        addComponents(statusLabel, statusLobbyLabel, hostStartButton, hostSessionIDClipboard)
    }

    override fun refreshAfterPlayerJoin() {
        val listOfPlayers = rootService.network.client?.playersNames
        checkNotNull(listOfPlayers)
        if( !hostLobby && playerNameInput.text == listOfPlayers[0] ){
            remove(backCount)
            addComponents(hostDisconnected)
        }
        removeComponents(player1LobbyLabel, player2LobbyLabel, player3LobbyLabel)
        if( rootService.network.client != null && listOfPlayers.size > 0){
            player1LobbyLabel.text = "Player1: " + listOfPlayers[0]
            addComponents(player1LobbyLabel)
        }
        if(listOfPlayers.size >= 2){
            player2LobbyLabel.text = "Player2: " + listOfPlayers[1]
            addComponents(player2LobbyLabel)
        }
        if(listOfPlayers.size >= 3) {
            player3LobbyLabel.text = "Player3: " + listOfPlayers[2]
            addComponents(player3LobbyLabel)
        }
        if(hostLobby && listOfPlayers.size >= 2){
            statusLobbyLabel.text = "GAME READY TO BE STARTED"
        }
        if(hostLobby && listOfPlayers.size < 2){
            statusLobbyLabel.text = "WAITING FOR PLAYERS TO CONNECT TO LOBBY"
        }
    }

    override fun refreshAfterPlayerDisconnect() {
        refreshAfterPlayerJoin()
    }

}