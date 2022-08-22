package view

import entity.*
import service.RootService
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.*
import tools.aqua.bgw.core.*
import tools.aqua.bgw.event.MouseEvent
import tools.aqua.bgw.event.ScrollDirection
import tools.aqua.bgw.util.CoordinatePlain
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*
import java.awt.Color
import javax.xml.crypto.dsig.Transform
import kotlin.math.*

const val TRAIN_CARDS: String = "GameScene/Cards/Train/"
const val DEST_CARDS: String = "GameScene/Cards/Destination/"

/**
 * Manages Game UI and service calls
 * @param root Grants access to service-layer
 */
@Suppress("UNCHECKED_CAST")
class GameScene(private val root: RootService) : BoardGameScene(1920, 1080), Refreshable {
    private var tunnelRoute: Triple<Tunnel, Int, Array<Any>>? = null
    private val playerBanner: Pane<UIComponent> = Pane( 0, 480, 1920, 600 )

    //<editor-fold desc="Player banner UI">
    private val playerTrainCarLabel: Label = Label(
        posX = 1636, posY = 537, width = 100, font = Font(size = 20, fontWeight = Font.FontWeight.BOLD)
    )

    private val currentPlayerImage: Label = Label( posX = 1720, posY = 425, width = 180, height = 180 )

    private val currentPlayerPoints: Label = Label(
        posX = 1720, posY = 530, width = 180, font = Font(size = 28, fontWeight = Font.FontWeight.BOLD)
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
    ).apply { onMouseClicked = { showCards(root.game.currentState.currentPlayer) } }

    private val otherPlayers: MutableList<Pane<UIComponent>> = mutableListOf()
    //</editor-fold>

    private val map: Pane<UIComponent> = Pane(
        20, 10, 675, 1047, ImageVisual("GameScene/map.jpg")
    )

    //<editor-fold desc="Show Train Cards">
    private var trainCardsToView: Array<CardView> = arrayOf()
    private var trainCardIndex: Int = 0

    private val selectedTrainCards: MutableList<Int> = mutableListOf()

    private fun setTrainCards(cards: Collection<CardView>): Unit {
        trainCardsToView = cards.toTypedArray();
        setTrainCardIndex(0)

        setCardsScrollable(trainCardsToView, trainCardIndex, trainScrollLeft, trainScrollRight)
    }

    private fun setTrainCardIndex(index: Int): Unit {
        trainCardIndex = min(max(0, index), max(0, trainCardsToView.size - 5))

        showTrainCards.clear()
        for(count in trainCardIndex..min (trainCardIndex + 4, trainCardsToView.size - 1)) {
            trainCardsToView[count].onMouseClicked = {
                val me = trainCardsToView[count]

                if(me.opacity == 1.0) {
                    me.opacity = 0.5
                    selectedTrainCards.add(count)
                } else {
                    me.opacity = 1.0
                    selectedTrainCards.remove(count)
                }
            }

            showTrainCards.add(trainCardsToView[count])
        }
    }

    private val showTrainCards: LinearLayout<CardView> = LinearLayout<CardView>(
        posX = 725, posY = 350, width = 850, height = 248, spacing = 2, alignment = Alignment.CENTER
    ).apply {
        onScroll = {
            if(it.direction == ScrollDirection.UP) {
                setTrainCardIndex(trainCardIndex + 5)
            } else {
                setTrainCardIndex(trainCardIndex - 5)
            }

            setCardsScrollable(trainCardsToView, trainCardIndex, trainScrollLeft, trainScrollRight)
        }
    }

    private val trainScrollLeft: Button = Button(
        width = 89, height = 89, posX = 725, posY = 430,
        visual = ImageVisual("GameScene/Buttons/scroll_left.png")
    ).apply {
        onMouseClicked = {
            setTrainCardIndex(trainCardIndex - 5)
            setCardsScrollable(trainCardsToView, trainCardIndex, this, trainScrollRight)
        }
    }

    private val trainScrollRight: Button = Button(
        width = 89, height = 89, posX = 1486, posY = 430,
        visual = ImageVisual("GameScene/Buttons/scroll_right.png")
    ).apply {
        onMouseClicked = {
            setTrainCardIndex(trainCardIndex + 5)
            setCardsScrollable(trainCardsToView, trainCardIndex, trainScrollLeft, this)
        }
    }
    //</editor-fold>

    //<editor-fold desc="Show Destination ('Dest') Cards">
    private var destCardsToView: Array<CardView> = arrayOf()
    private var destCardIndex: Int = 0

    private val selectedDestCards: MutableList<Int> = mutableListOf()

    private fun setDestCards(cards: Collection<CardView>): Unit {
        destCardsToView = cards.toTypedArray();
        setDestCardIndex(0)

        setCardsScrollable(destCardsToView, destCardIndex, destScrollLeft, destScrollRight)
    }

    private fun setDestCardIndex(index: Int): Unit {
        destCardIndex = min(max(0, index), max(0, destCardsToView.size - 5))

        showDestCards.clear()
        for(count in destCardIndex..min (destCardIndex + 4, destCardsToView.size - 1)) {
            destCardsToView[count].onMouseClicked = {
                val me = destCardsToView[count]

                if(me.opacity == 1.0) {
                    me.opacity = 0.5
                    selectedDestCards.add(count)
                } else {
                    me.opacity = 1.0
                    selectedDestCards.remove(count)
                }
            }
            showDestCards.add(destCardsToView[count])
        }
    }

    private val showDestCards: LinearLayout<CardView> = LinearLayout<CardView>(
        posX = 725, posY = 600, width = 850, height = 248, spacing = 2, alignment = Alignment.CENTER
    ).apply {
        onScroll = {
            if(it.direction == ScrollDirection.UP) {
                setDestCardIndex(destCardIndex + 5)
            } else {
                setDestCardIndex(destCardIndex - 5)
            }

            setCardsScrollable(destCardsToView, destCardIndex, destScrollLeft, destScrollRight)
        }
    }

    private val destScrollLeft: Button = Button(
        width = 89, height = 89, posX = 725, posY = 680,
        visual = ImageVisual("GameScene/Buttons/scroll_left.png")
    ).apply {
        onMouseClicked = {
            setDestCardIndex(destCardIndex - 5)
            setCardsScrollable(destCardsToView, destCardIndex, this, destScrollRight)
        }
    }

    private val destScrollRight: Button = Button(
        width = 89, height = 89, posX = 1486, posY = 680,
        visual = ImageVisual("GameScene/Buttons/scroll_right.png")
    ).apply {
        onMouseClicked = {
            setDestCardIndex(destCardIndex + 5)
            setCardsScrollable(destCardsToView, destCardIndex, destScrollLeft, this)
        }
    }
    //</editor-fold>

    //<editor-fold desc="Card Deck">
    private val viewingLabel: Label = Label(
        posX = 725, posY = 870, width = 500, text = "You are viewing the cards of: ",
        font = Font(size = 28, color = Color.WHITE)
    )
    private val viewingImage: Label = Label( posX = 1160, posY = 858, width = 65, height = 65 )

    private val destCardDeck: Button = Button(
        posX = 1670, posY = 0, width = 245, height = 163
    ).apply { onMouseClicked = { focusDrawDestCards() } }
    private val openTrainCards: LinearLayout<CardView> = LinearLayout<CardView>(
        posX = 1460, posY = 355, width = 670, height = 200, spacing = 5, alignment = Alignment.CENTER
    ).apply { rotation = 270.0 }
    private val trainCardDeck: Button = Button(
        posX = 1670, posY = 768, width = 245, height = 160
    ).apply { onMouseClicked = { root.playerActionService.drawWagonCard(-1) } }
    //</editor-fold>

    //<editor-fold desc="Focus Elements">
    private val leftFocus: Label = Label(visual = ColorVisual.BLACK).apply { opacity = 0.75 }
    private val topFocus: Label = Label(visual = ColorVisual.BLACK).apply { opacity = 0.75 }
    private val bottomFocus: Label = Label(visual = ColorVisual.BLACK).apply { opacity = 0.75 }
    private val rightFocus: Label = Label(visual = ColorVisual.BLACK).apply { opacity = 0.75 }
    private val focusButton: Button = Button(
        width = 800, height = 75, posX = 1100, posY = 975, visual = ImageVisual("wood_btn.jpg"),
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE)
    )
    private val focusPlayer: Label = Label(width = 156, height = 156, posX = 822, posY = 5)
    //</editor-fold>

    init {
        opacity = 1.0
        background = ImageVisual("GameScene/background.png")

        playerBanner.addAll(playerTrainCarLabel, currentPlayerImage, currentPlayerPoints, redo, undo, showCurrentPlayerCards)

        addComponents(
            playerBanner, map,
            showTrainCards, trainScrollLeft, trainScrollRight,
            showDestCards, destScrollLeft, destScrollRight,
            viewingLabel, viewingImage,
            destCardDeck, openTrainCards, trainCardDeck
        )

        buildMapButtons()
        initializeOtherPlayerUI()
        updateDecks()

        focusChooseDestCards(0)
    }

    private fun City.findRoute(to: City): Route = checkNotNull(routes.find {
        (it.cities.first === this && it.cities.second === to)
                || (it.cities.first === to && it.cities.second === this)
    })

    private fun buildMapButtons() {
        for(route in mapRouteButtons) {
            for(fieldIndex in 0..route.size-3) {
                val transform = route[fieldIndex] as Pair<Triple<Int, Int, Double>, Boolean>

                map.add(Button(
                    posX = transform.first.first, posY = transform.first.second, width = 36, height = 13,
                    visual = ColorVisual.TRANSPARENT
                ).apply {
                    componentStyle = "-fx-background-insets: 0 0 12 0;"
                    rotation = transform.first.third

                    val stations = route.last() as Pair<String, String>
                    name = stations.first + " - " + stations.second

                    onMouseClicked = {
                        val cities = root.game.currentState.cities.associateBy { it.name }
                        val gameRoute = checkNotNull(cities[stations.first])
                            .findRoute(checkNotNull(cities[stations.second]))

                        var claimSuccess: Boolean = true
                        val playerIndex: Int = root.game.currentState.currentPlayerIndex

                        try{
                            root.playerActionService.claimRoute(gameRoute,
                                root.game.currentState.currentPlayer.wagonCards.slice(selectedTrainCards))
                        } catch (e: Exception) {
                            focusErrorMessage("An error occurred while claiming the route:\n" + e.message)
                            claimSuccess = false
                        }

                        if(claimSuccess) {
                            if(gameRoute is Tunnel) {
                                tunnelRoute = Triple(gameRoute, route[route.size - 2] as Int,
                                    route)
                            } else {
                                val routeStart = route[route.size - 2] as Int
                                for (mapIndex in routeStart..routeStart + route.size - 3) {
                                    map.elementAt(mapIndex).visual = ImageVisual(
                                        path = getBoardFieldPath(
                                            (route[mapIndex - routeStart] as Pair<Any, Boolean>).second,
                                            playerIndex
                                        )
                                    )
                                    map.elementAt(mapIndex).isDisabled = true
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getBoardFieldPath(isTrain: Boolean, playerIndex: Int): String {
        return when(isTrain) {
            false -> getPlayerFolder(playerIndex) + "wagon.png"
            true -> getPlayerFolder(playerIndex) + "train.png"
        }
    }

    private fun getPlayerFolder(index: Int): String {
        return when(index) {
            0 -> "GameScene/Player/Yellow/"
            1 -> "GameScene/Player/Purple/"
            else -> "GameScene/Player/Red/"
        }
    }

    private fun showCards(playerToExpose: Player): Unit {
        val trainCards: MutableList<CardView> = mutableListOf()
        val destCards: MutableList<CardView> = mutableListOf()

        for(wagonIndex in playerToExpose.wagonCards.indices) {
            trainCards.add(CardView(
                front = ImageVisual(TRAIN_CARDS + playerToExpose.wagonCards[wagonIndex].color.toString() + ".png")
            ).apply {
                if(playerToExpose != root.game.currentState.currentPlayer ||
                    playerToExpose is AIPlayer || playerToExpose.isRemote)
                    isDisabled = true
                else {
                    if(selectedTrainCards.contains(wagonIndex))
                        opacity = 0.5
                }
            })
        }
        for(dest in playerToExpose.destinationCards) {
            val start: String = dest.cities.first.name.lowercase().
                replace('å', 'a').replace('ø', 'o').
                replace('ö', 'o').replace(" ", "")
            val end: String = dest.cities.second.name.lowercase().
                replace('å', 'a').replace('ø', 'o').
                replace('ö', 'o').replace(" ", "")

            destCards.add(CardView(front = ImageVisual("$DEST_CARDS$start-$end.png")).apply {
                if(root.game.gameState != GameState.CHOOSE_DESTINATION_CARD ||
                    playerToExpose is AIPlayer || playerToExpose.isRemote)
                    isDisabled = true
            })
        }

        for(index in root.game.currentState.players.indices) {
            if(root.game.currentState.players[index] == playerToExpose) {
                viewingImage.visual = ImageVisual(getPlayerFolder(index) + "player_profile.png")
                break
            }
        }

        setTrainCards(trainCards)
        setDestCards(destCards)
    }

    private fun setCardsScrollable(cards: Array<CardView>, currentIndex: Int, left: Button, right: Button): Unit {
        left.isDisabled = (currentIndex == 0)
        left.opacity = when(left.isDisabled) { true -> 0.5; false -> 1.0 }

        right.isDisabled = (cards.size == currentIndex + 5 || cards.size <= 5)
        right.opacity = when(right.isDisabled) { true -> 0.5; false -> 1.0 }
    }

    private fun initializeOtherPlayerUI(): Unit {
        if(root.game.currentState.players.size == 2) {
            otherPlayers.add(Pane(1025, 75, 250, 250))
        } else {
            otherPlayers.add(Pane(725, 75, 250, 250))
            otherPlayers.add(Pane(1350, 75, 250, 250))
        }

        for(playerUI in otherPlayers) {
            val trainCarImage: Label = Label( posX = 10, posY = 90, width = 70, height = 70 )
            val trainCarLabel: Label = Label(
                posX = 10, posY = 125, width = 70, font = Font(size = 20, fontWeight = Font.FontWeight.BOLD)
            )
            val playerImage: Label = Label( posX = 60, posY = 0, width = 180, height = 180 )
            val playerPoints: Label = Label(
                posX = 60, posY = 105, width = 180, font = Font(size = 28, fontWeight = Font.FontWeight.BOLD)
            )
            val showPlayerCards: Button = Button(
                width = 250, height = 70, posY = 180, posX = 0, visual = ImageVisual("wood_btn.jpg"),
                font = Font(size = 28, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE), text = "Show Cards"
            )

            playerUI.addAll(trainCarImage, trainCarLabel, playerImage, playerPoints, showPlayerCards)
            addComponents(playerUI)
        }

        setPlayerImages()
    }

    private fun setPlayerImages(): Unit {
        val currentPlayerIndex = root.game.currentState.currentPlayerIndex

        //Set current player images
        playerBanner.visual = ImageVisual(getPlayerFolder(currentPlayerIndex) + "dock.png")
        playerTrainCarLabel.text = root.game.currentState.currentPlayer.trainCarsAmount.toString()
        currentPlayerImage.visual = ImageVisual(getPlayerFolder(currentPlayerIndex) + "player_profile.png")
        currentPlayerPoints.text = root.game.currentState.currentPlayer.points.toString()

        val playerIndices: MutableList<Int> = mutableListOf()
        if(root.game.currentState.players.size == 2){
            playerIndices.add((root.game.currentState.currentPlayerIndex + 1) % 2)
        } else {
            playerIndices.add((root.game.currentState.currentPlayerIndex + 1) % 3)
            playerIndices.add((root.game.currentState.currentPlayerIndex + 2) % 3)
        }

        for(count in otherPlayers.indices) {
            (otherPlayers[count].components[0] as Label).visual =
                ImageVisual(getPlayerFolder(playerIndices[count]) + "train_number.png")
            (otherPlayers[count].components[1] as Label).text =
                root.game.currentState.players[playerIndices[count]].trainCarsAmount.toString()
            (otherPlayers[count].components[2] as Label).visual =
                ImageVisual(getPlayerFolder(playerIndices[count]) + "player_profile.png")
            (otherPlayers[count].components[3] as Label).text =
                root.game.currentState.players[playerIndices[count]].points.toString()
            (otherPlayers[count].components[4] as Button).onMouseClicked =
                { showCards(root.game.currentState.players[playerIndices[count]]) }
        }
    }

    private fun updateDecks() {
        if(root.game.currentState.destinationCards.isNotEmpty()) {
            val destDeckSize: Int = min((root.game.currentState.destinationCards.size / 10 * 10) + 10, 100)
            destCardDeck.visual = ImageVisual(DEST_CARDS + "Back/back-" + destDeckSize.toString() + ".png")
        } else {
            destCardDeck.visual = Visual.EMPTY
            destCardDeck.isDisabled = true
        }

        if(root.game.currentState.wagonCardsStack.isNotEmpty()) {
            val trainDeckSize: Int = min((root.game.currentState.wagonCardsStack.size / 10 * 10) + 10, 100)
            trainCardDeck.visual = ImageVisual(TRAIN_CARDS + "Back/back-" + trainDeckSize.toString() + ".png")
        } else {
            trainCardDeck.visual = Visual.EMPTY
            trainCardDeck.isDisabled = true
        }

        openTrainCards.clear()
        for (openCard in root.game.currentState.openCards) {
            openTrainCards.add(CardView( width = 120, height = 186,
                front = ImageVisual(TRAIN_CARDS + openCard.color.toString() + ".png")).apply {
                    onMouseClicked = { root.playerActionService.drawWagonCard(openTrainCards.indexOf(this)) }
            })
        }
    }

    //<editor-fold desc="Focus Functions">
    private fun focusUI(toFocus: Any, focusText: String, playerIndex: Int, focusAction: (MouseEvent) -> Unit): Unit {
        val focus = when(toFocus){
            is UIComponent -> toFocus
            is LinearLayout<*> -> toFocus
            else -> Label(posX = 1920, posY = 1080, width = 0, height = 0)
        }

        leftFocus.apply { posX = 0.0; posY = 0.0; height = 1080.0; width = focus.posX }
        topFocus.apply { posX = focus.posX; posY = 0.0; height = focus.posY; width = focus.width }
        bottomFocus.apply { posX = focus.posX; posY = focus.posY + focus.height;
            height = 1080 - focus.posY - focus.height; width = focus.width }
        rightFocus.apply { posX = focus.posX + focus.width; posY = 0.0;
            height = 1080.0; width = 1920 - focus.posX - focus.width }

        if(!focusText.isEmpty()) {
            focusButton.text = focusText
            focusButton.onMouseClicked = focusAction
        }

        focusPlayer.visual = ImageVisual(getPlayerFolder(playerIndex) + "player_profile.png")

        addComponents(leftFocus, topFocus, bottomFocus, rightFocus, focusButton, focusPlayer)
    }

    private fun unFocus(): Unit {
        removeComponents(focusButton, leftFocus, topFocus, bottomFocus, rightFocus, focusPlayer)
    }

    private fun focusChooseDestCards(playerIndex: Int): Unit {
        if(playerIndex < 0 || playerIndex >= root.game.currentState.players.size) {
            focusUI(0, "Wait for other players...", 0) {}
            return
        }

        val focusPlayer: Player = root.game.currentState.players[playerIndex]

        if(focusPlayer is AIPlayer || focusPlayer.isRemote) {
            focusChooseDestCards( playerIndex + 1)
        }

        selectedDestCards.clear()

        showCards(focusPlayer)
        focusUI(showDestCards, "Choose at least two cards and continue...", playerIndex) {
            if(selectedDestCards.size >= 2) {
                unFocus()
                focusChooseDestCards(playerIndex + 1)
                root.gameService.chooseDestinationCards(selectedDestCards)
            }
        }
    }

    private fun focusErrorMessage(message: String) {
        val errorMessage: Label = Label(
            width = 1000, height = 500, posY = 290, posX = 460, visual = ImageVisual("wood_btn.jpg"),
            font = Font(size = 28, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE), text = message
        )
        addComponents(errorMessage)
        println(message)

        focusUI(errorMessage, "Continue", root.game.currentState.currentPlayerIndex) {
            removeComponents(errorMessage)
            unFocus()
        }
    }

    private fun focusDrawDestCards() {
        val drawDestCards: MutableList<Int> = mutableListOf()

        val destCardsToDraw: LinearLayout<CardView> = LinearLayout<CardView>(
            posX = 625, posY = 440, width = 670, height = 200, spacing = 5,
            alignment = Alignment.CENTER, visual = ImageVisual("wood_btn.jpg")
        ).apply {
            val destCardStack = root.game.currentState.destinationCards
            val cardsToDraw = destCardStack.subList(max(0, destCardStack.size - 3), destCardStack.size)

            for (index in cardsToDraw.indices) {
                val start: String = cardsToDraw[index].cities.first.name.lowercase().
                replace('å', 'a').replace('ø', 'o').
                replace('ö', 'o').replace(" ", "")
                val end: String = cardsToDraw[index].cities.second.name.lowercase().
                replace('å', 'a').replace('ø', 'o').
                replace('ö', 'o').replace(" ", "")

                add(CardView(front = ImageVisual("$DEST_CARDS$start-$end.png")).apply {
                    onMouseClicked = {
                        if(opacity == 1.0) {
                            opacity = 0.5
                            drawDestCards.add(index)
                        }
                        else {
                            opacity = 1.0
                            drawDestCards.remove(index)
                        }
                    }
                })
            }
        }

        addComponents(destCardsToDraw)

        focusUI(destCardsToDraw, "Draw selected cards", root.game.currentState.currentPlayerIndex) {
            if(drawDestCards.size > 0) {
                root.playerActionService.drawDestinationCards(drawDestCards)
                removeComponents(destCardsToDraw)
                unFocus()
            }
        }
    }

    private fun focusPayTunnel(): Unit {
        selectedTrainCards.clear()
        showCards(root.game.currentState.currentPlayer)

        focusUI(showTrainCards, "Pay for the tunnel", root.game.currentState.currentPlayerIndex) {
            val tunnel = tunnelRoute
            checkNotNull(tunnel)
            if(selectedTrainCards.size > 0) {
                try {
                    root.playerActionService.afterClaimTunnel(
                        tunnel.first,
                        root.game.currentState.currentPlayer.wagonCards.slice(selectedTrainCards)
                    )
                } catch (e: Exception) {
                    focusButton.text = e.message + " Pay for the tunnel"
                }
            } else {
                root.playerActionService.afterClaimTunnel(tunnel.first, null)
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Refreshables">
    override fun refreshAfterChooseDestinationCard() {
        unFocus()
        showCards(root.game.currentState.currentPlayer)
    }

    override fun refreshAfterNextPlayer() {
        if(components.contains(topFocus))
            unFocus()

        selectedTrainCards.clear()
        selectedDestCards.clear()

        setPlayerImages()
        showCards(root.game.currentState.currentPlayer)
    }

    override fun refreshAfterUndoRedo() {
        refreshAfterNextPlayer()
    }

    override fun refreshAfterDrawWagonCards() {
        if(root.game.gameState == GameState.DREW_WAGON_CARD) {
            focusUI(
                Label(
                    posX = trainCardDeck.posX, posY = destCardDeck.posY + destCardDeck.height,
                    width = trainCardDeck.width,
                    height = trainCardDeck.posY - (destCardDeck.posY + destCardDeck.height) + trainCardDeck.height
                ), "Choose another card",
                root.game.currentState.currentPlayerIndex
            ) {}
        }
        else
            unFocus()

        updateDecks()
    }

    override fun refreshAfterDrawDestinationCards() {
        updateDecks()
    }

    override fun refreshAfterClaimRoute() {
        if(root.game.gameState == GameState.AFTER_CLAIM_TUNNEL) {
            showCards(root.game.currentState.currentPlayer)
            focusPayTunnel()
        }
    }

    override fun refreshAfterAfterClaimTunnel() {
        unFocus()

        val tunnel = tunnelRoute
        checkNotNull(tunnel)
        val routeStart = tunnel.second
        for (mapIndex in routeStart..routeStart + tunnel.third.size - 3) {
            map.elementAt(mapIndex).visual = ImageVisual(
                path = getBoardFieldPath(
                    (tunnel.third[mapIndex - routeStart] as Pair<Any, Boolean>).second,
                    root.game.currentState.currentPlayerIndex
                )
            )
            map.elementAt(mapIndex).isDisabled = true
        }
    }
    //</editor-fold>
}