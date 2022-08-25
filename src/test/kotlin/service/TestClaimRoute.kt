package service

import entity.*
import java.lang.Integer.max
import java.util.*
import kotlin.test.*

/**
 * Tests the [PlayerActionService.claimRoute] and [PlayerActionService.afterClaimTunnel]
 * methods
 */
class TestClaimRoute {
    private fun PlayerActionService.assertFailedClaim(route: Route, cards: List<WagonCard>) {
        val state = root.game.currentState
        assertNotNull(claimRoute(route, cards))
        assertSame(state, root.game.currentState)
    }

    private fun PlayerActionService.assertFailedTunnelClaim(
        route: Route,
        originalCards: List<WagonCard>,
        newCards: List<WagonCard>
    ) {
        claimRoute(route, originalCards)
        val state = root.game.currentState
        assertFails {
            afterClaimTunnel(route as Tunnel, newCards)
        }
        assertSame(state, root.game.currentState)
        root.game.gameState = GameState.DEFAULT
    }

    private fun State.discardStackContains(cards: List<WagonCard>, exhaustive: Boolean) {
        val discard = IdentityHashMap<WagonCard, Unit>(discardStack.size)
        for (card in discardStack) {
            assert(discard.put(card, Unit) == null)
        }
        for (card in cards) {
            assert(discard.remove(card) != null)
        }
        //exhaustive implies discard is empty
        assert(!exhaustive || discard.isEmpty())
    }

    private fun PlayerActionService.assertClaimRouteSuccess(
        route: Route,
        usedCards: List<WagonCard>,
        newPoints: Int,
        newTrainCars: Int,
    ) = assertClaimRouteSuccess(route, usedCards, newPoints, newTrainCars) {
            route1, usedCards1 -> claimRoute(route1, usedCards1)
    }

    private fun PlayerActionService.assertClaimTunnelSuccess(
        route: Route,
        usedCards: List<WagonCard>,
        requiredCards: List<WagonCard>,
        newPoints: Int,
        newTrainCars: Int,
    ) {
        assert(root.game.currentState.run { wagonCardsStack.size >= 3 || discardStack.isEmpty() }) {
            "Method does not test redrawing"
        }
        val (newDraw, reqCards) = root.game.currentState.wagonCardsStack.run {
            subList(0, max(0, size - 3)) to subList(max(0, size - 3), size)
        }
        val oldIdx = root.game.currentState.currentPlayerIndex
        val oldCards = root.game.currentState.currentPlayer.wagonCards
        val oldRoutes = root.game.currentState.currentPlayer.claimedRoutes
        val oldDiscard = root.game.currentState.discardStack
        claimRoute(route, usedCards)
        afterClaimTunnel(route as Tunnel, requiredCards)
        root.game.currentState.players[oldIdx].run {
            assertEquals(newPoints, points)
            assertEquals(newTrainCars, trainCardsAmount)
            val playerHandCards = IdentityHashMap<WagonCard, Unit>(oldCards.size)
            oldCards.forEach { assert(playerHandCards.put(it, Unit) == null) }
            usedCards.forEach { assert(playerHandCards.remove(it) != null) }
            requiredCards.forEach { assert(playerHandCards.remove(it) != null) }
            wagonCards.forEach { assert(playerHandCards.remove(it) != null) }
            assert(playerHandCards.isEmpty())
            val routes = IdentityHashMap<Route, Unit>(claimedRoutes.size)
            claimedRoutes.forEach { assert(routes.put(it, Unit) == null) }
            oldRoutes.forEach { assert(routes.remove(it) != null) }
            assert(routes.remove(route) != null)
            assert(routes.isEmpty())
        }
        root.game.currentState.discardStackContains(oldDiscard + usedCards + reqCards, false)
        val newDrawStack = root.game.currentState.wagonCardsStack
        assert(newDraw.size == newDrawStack.size)
        assert(newDraw.zip(newDrawStack).all { it.first === it.second })
    }

    private fun PlayerActionService.assertClaimRouteSuccess(
        route: Route,
        usedCards: List<WagonCard>,
        newPoints: Int,
        newTrainCars: Int,
        claim: PlayerActionService.(Route, List<WagonCard>) -> Unit
    ) {
        val oldIdx = root.game.currentState.currentPlayerIndex
        val oldCards = root.game.currentState.currentPlayer.wagonCards
        val oldRoutes = root.game.currentState.currentPlayer.claimedRoutes
        val oldDiscard = root.game.currentState.discardStack
        claim(route, usedCards)
        root.game.currentState.players[oldIdx].run {
            assertEquals(newPoints, points)
            assertEquals(newTrainCars, trainCardsAmount)
            val playerHandCards = IdentityHashMap<WagonCard, Unit>(oldCards.size)
            oldCards.forEach { assert(playerHandCards.put(it, Unit) == null) }
            usedCards.forEach { assert(playerHandCards.remove(it) != null) }
            wagonCards.forEach { assert(playerHandCards.remove(it) != null) }
            assert(playerHandCards.isEmpty())
            val routes = IdentityHashMap<Route, Unit>(claimedRoutes.size)
            claimedRoutes.forEach { assert(routes.put(it, Unit) == null) }
            oldRoutes.forEach { assert(routes.remove(it) != null) }
            assert(routes.remove(route) != null)
            assert(routes.isEmpty())
        }
        root.game.currentState.discardStackContains(oldDiscard + usedCards, true)
    }

    /**
     * Tests whether [PlayerActionService.claimRoute] fails when it is supposed to on a normal route
     */
    @Test
    fun testClaimRouteFail() {
        val root = RootService()
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(), listOf(WagonCard(Color.RED)), 40, emptyList(), false),
                Player(0, "abv", emptyList(), emptyList(), 40, emptyList(), false)
            ),
            cities = constructGraph(),
        )
        root.game = Game(state1)
        val cities = state1.cities.associateBy { it.name }
        val hel = checkNotNull(cities["Helsinki"])
        val ima = checkNotNull(cities["Imatra"])
        val lah = checkNotNull(cities["Lahti"])
        val kuo = checkNotNull(cities["Kuopio"])
        val oul = checkNotNull(cities["Oulu"])
        assertFails {
            root.playerActionService.claimRoute(hel.findRoute(ima),
                List(3) { state1.players.first().wagonCards.first() }
            )
        }
        val newPlayerCards =
            listOf(WagonCard(Color.JOKER)) +
                    List(4) { WagonCard(Color.RED) } +
                    List(3) { WagonCard(Color.BLUE) } +
                    List(3) { WagonCard(Color.JOKER) }
        val state2 = state1.updatedPlayer {
            copy(wagonCards = newPlayerCards)
        }
        root.insert(state2)
        assertFails {
            root.playerActionService.claimRoute(hel.findRoute(ima), List(3) { WagonCard(Color.RED) })
        }
        assertFails {
            root.playerActionService.claimRoute(hel.findRoute(lah), List(3) { WagonCard(Color.BLACK) })
        }
        //2x Red
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(1, 3))
        //4x Red
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(1, 5))
        //2x Red + 1x Blue
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(4, 7))
        //3x BLue
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(5, 8))
        //1x Locomotive + 2x Red
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(0, 3))
        //2x Red, 3x Blue
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(3, 8))
        //3x Locomotives
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(8, 11))
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), emptyList())
        //2x Red, 1x Blue
        root.playerActionService.assertFailedClaim(oul.findRoute(kuo), newPlayerCards.subList(3, 6))
        //1x Locomotive, 2x Red
        root.playerActionService.assertFailedClaim(oul.findRoute(kuo), newPlayerCards.subList(0, 3))
        //2x Locomotive, 3x Blue
        root.playerActionService.assertFailedClaim(oul.findRoute(kuo), newPlayerCards.subList(4, 9))
        val state3 = state2.updatedPlayer(1) { copy(claimedRoutes = listOf(hel.findRoute(ima))) }
        root.insert(state3)
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(1, 3))
        val state4 = state2.updatedPlayer { copy(claimedRoutes = listOf(hel.findRoute(ima))) }
        root.insert(state4)
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(1, 3))
    }

    /**
     * Tests whether [PlayerActionService.claimRoute] fails when it is supposed to on a ferry route
     */
    @Test
    fun testClaimFerryFail() {
        val root = RootService()
        val cards = List(4) { WagonCard(Color.GREEN) }
            .plus(List(2) { WagonCard(Color.JOKER) })
            .plus(List(2) { WagonCard(Color.PURPLE) })
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(
                    0, "abc", emptyList(),
                    wagonCards = cards,
                    40, emptyList(), false
                )
            ),
            cities = constructGraph(),
        )
        root.game = Game(state1)
        val cities = state1.cities.associateBy { it.name }
        val tal = checkNotNull(cities["Tallinn"])
        val sto = checkNotNull(cities["Stockholm"])
        //4x Green
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(0, 4))
        //3x green + 1x locomotive
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(1, 5))
        //4x green + 2x locomotive
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(0, 6))
        //2x locomotives + 2x purple
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(4, 8))
        //1xGreen, 1xPurple, 2xLocomotive
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(3, 7))
        //2x Locomotive
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(4, 6))
        //3x Green
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(0, 3))
        //4x Green
        root.playerActionService.assertFailedClaim(tal.findRoute(sto), state1.currentPlayer.wagonCards.subList(0, 5))
    }

    /**
     * Tests whether [PlayerActionService.claimRoute] fails on insufficient [Player.trainCardsAmount]
     */
    @Test
    fun testInsufficientTrainCars() {
        val root = RootService()
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(), List(3) { WagonCard(Color.RED) }, 2, emptyList(), false)
            ),
            cities = constructGraph(),
        )
        root.game = Game(state1)
        val cities = state1.cities.associateBy { it.name }
        val hel = checkNotNull(cities["Helsinki"])
        val ima = checkNotNull(cities["Imatra"])
        //Fails because insufficient amount of train cars
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), state1.currentPlayer.wagonCards)
    }

    /**
     * Tests whether [PlayerActionService.claimRoute] works correctly with double routes
     */
    @Test
    fun testDoubleRoute() {
        val root = RootService()
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(), List(3) { WagonCard(Color.RED) }, 40, emptyList(), false),
                Player(0, "avg", emptyList(), emptyList(), 40, emptyList(), false)
            ),
            cities = constructGraph(),
        )
        root.game = Game(state1)
        val cities = state1.cities.associateBy { it.name }
        val bod = checkNotNull(cities["Boden"])
        val ume = checkNotNull(cities["Umeå"])
        val umeBodRed: Route
        val umeBodWhite: Route
        run {
            val route = ume.findRoute(bod)
            if (route.color == Color.RED) {
                umeBodRed = route
                umeBodWhite = checkNotNull(route.sibling)
            } else {
                umeBodRed = checkNotNull(route.sibling)
                umeBodWhite = route
            }
        }
        fun assertClaimRouteSuccess() {
            val state = root.game.currentState
            root.playerActionService.claimRoute(umeBodRed, state1.currentPlayer.wagonCards)
            root.game.currentState.players[0].run {
                assertEquals(4, points)
                assertEquals(37, trainCardsAmount)
                assertEquals(emptyList(), wagonCards)
            }
            root.game.currentState.discardStackContains(state1.currentPlayer.wagonCards, exhaustive = true)
            root.undo()
            assertSame(state, root.game.currentState)
        }
        assertClaimRouteSuccess()
        val state2 = state1.updatedPlayer(1) { copy(claimedRoutes = listOf(umeBodWhite)) }
        root.insert(state2)
        root.playerActionService.assertFailedClaim(umeBodRed, state1.currentPlayer.wagonCards)
        val state3 = state2.copy(
            players = state2.players + Player(0, "jk", emptyList(), emptyList(), 2, emptyList(), false)
        )
        root.insert(state3)
        assertClaimRouteSuccess()
        val state4 = state3.updatedPlayer(1) { copy(claimedRoutes = emptyList()) }
        root.insert(state4)
        assertClaimRouteSuccess()
        val state5 = state4.updatedPlayer { copy(claimedRoutes = listOf(umeBodWhite)) }
        root.insert(state5)
        root.playerActionService.assertFailedClaim(umeBodRed, state1.currentPlayer.wagonCards)
        val state6 = state4.updatedPlayer { copy(claimedRoutes = listOf(umeBodRed)) }
        root.insert(state6)
        root.playerActionService.assertFailedClaim(umeBodRed, state1.currentPlayer.wagonCards)
    }

    /**
     * Tests whether [PlayerActionService.claimRoute] works correctly when it is supposed to
     */

    @Test
    fun testClaimRouteSuccess() {
        val root = RootService()
        val playerCards = List(3) { WagonCard(Color.RED) } +
                List(6) { WagonCard(Color.BLACK) } +
                List(5) { WagonCard(Color.JOKER) }
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(), playerCards, 40, emptyList(), false)
            ),
            cities = constructGraph(),
        )
        root.game = Game(state1)
        val cities = state1.cities.associateBy { it.name }
        val hel = checkNotNull(cities["Helsinki"])
        val ima = checkNotNull(cities["Imatra"])
        val ber = checkNotNull(cities["Bergen"])
        val and = checkNotNull(cities["Åndalsnes"])
        val kou = checkNotNull(cities["Kuopio"])
        val vaa = checkNotNull(cities["Vaasa"])
        root.playerActionService.assertClaimRouteSuccess(hel.findRoute(ima), playerCards.subList(0, 3), 4, 37)
        root.undo()
        //3x Black, 2x Locomotive
        root.playerActionService.assertClaimRouteSuccess(ber.findRoute(and), playerCards.subList(6, 11), 10, 35)
        root.undo()
        //6x Black, 3x Red
        root.playerActionService.assertClaimRouteSuccess(ber.findRoute(and), playerCards.subList(0, 9), 10, 35)
        root.undo()
        //2x Black, 3x Red, 2x Locomotive
        root.playerActionService.assertClaimRouteSuccess(
            ber.findRoute(and), playerCards.subList(0, 3) + playerCards.subList(7, 11), 10, 35
        )
        root.undo()
        //2x Black, 3x Locomotive
        root.playerActionService.assertClaimRouteSuccess(ber.findRoute(and), playerCards.subList(7, 12), 10, 35)
        root.undo()
        //5x Locomotive
        root.playerActionService.assertClaimRouteSuccess(ber.findRoute(and), playerCards.subList(9, 14), 10, 35)
        root.undo()
        //4x Locomotive
        root.playerActionService.assertClaimRouteSuccess(vaa.findRoute(kou), playerCards.subList(9, 13), 7, 36)
        root.undo()
    }

    /**
     * Tests whether a ferry can be successfully claimed
     */
    @Test
    fun testFerrySuccess() {
        val root = RootService()
        val playerCards = List(3) { WagonCard(Color.RED) } +
                List(6) { WagonCard(Color.BLACK) } +
                List(5) { WagonCard(Color.JOKER) }
        val state1 = State(emptyList(), emptyList(), emptyList(), emptyList(),
            listOf(
                Player(0, "abc", emptyList(), playerCards, 40, emptyList(), false)
            ),
            cities = constructGraph(),
        )
        root.game = Game(state1)
        val cities = state1.cities.associateBy { it.name }
        val got = checkNotNull(cities["Göteborg"])
        val aal = checkNotNull(cities["Ålborg"])
        root.playerActionService.assertClaimRouteSuccess(got.findRoute(aal), playerCards.subList(9, 11), 2, 38)
    }

    /**
     * Tests whether the murmansk lieksa route works correctly
     */
    @Test
    fun testMurmanskLieksa() {
        val root = RootService()
        val playerCards = List(9) { WagonCard(Color.RED) } +
                List(9) { WagonCard(Color.JOKER) }
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(), playerCards, 40, emptyList(), false)
            ),
            cities = constructGraph(),
        )
        root.game = Game(state1)
        val cities = state1.cities.associateBy { it.name }
        val mur = checkNotNull(cities["Murmansk"])
        val lie = checkNotNull(cities["Lieksa"])
        //9x Red
        root.playerActionService.assertClaimRouteSuccess(mur.findRoute(lie), playerCards.subList(0, 9), 27, 31)
        root.undo()
        // 9x Locomotive
        root.playerActionService.assertClaimRouteSuccess(mur.findRoute(lie), playerCards.subList(9, 18), 27, 31)
        root.undo()
        // 4x Red, 8x Locomotive
        root.playerActionService.assertClaimRouteSuccess(mur.findRoute(lie), playerCards.subList(5, 17), 27, 31)
        root.undo()
        // 3x Red, 8x Locomotive
        root.playerActionService.assertFailedClaim(mur.findRoute(lie), playerCards.subList(6, 17))
    }

    /**
     * Tests whether [PlayerActionService.claimRoute] and [PlayerActionService.afterClaimTunnel] works correctly with
     * a route
     */
    @Test
    fun testTunnel() {
        val playerCards = List(7) { WagonCard(Color.BLUE) } +
                List(7) { WagonCard(Color.JOKER) } +
                List(3) { WagonCard(Color.ORANGE) }
        val state = State(
            emptyList(), emptyList(), listOf(), emptyList(),
            listOf(
                Player(0, "adadsd", emptyList(), playerCards, isRemote = false),
                Player(0, "kfwrjfijfw", emptyList(), emptyList(), isRemote = false)
            ),
            cities = constructGraph()
        )
        val cities = state.cities.associateBy { it.name }
        val root = RootService()
        root.game = Game(state)

        fun RootService.setsDrawStack(newStack: List<WagonCard>) {
            insert(
                game.currentState.copy(
                    wagonCardsStack = newStack
                )
            )
        }

        val route = kotlin.run {
            val route = checkNotNull(cities["Bergen"]).findRoute(checkNotNull(cities["Oslo"]))
            if (route.color == Color.BLUE) {
                route
            } else {
                checkNotNull(route.sibling)
            }
        }

        //3x Locomotive, 3x Orange
        root.playerActionService.assertFailedClaim(route, playerCards.subList(5, 11))
        // 4x Blue, Pay with 3x Blue
        root.setsDrawStack(List(3) { WagonCard(Color.BLUE) })
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(0, 4), playerCards.subList(4, 7), 7, 36
        )
        root.undo()
        // 4x Blue, Pay with 3x Blue
        root.setsDrawStack(List(3) { WagonCard(Color.BLACK) } + List(3) { WagonCard(Color.BLUE) })
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(0, 4), playerCards.subList(4, 7), 7, 36
        )
        root.undo()
        //4x Blue, Pay with 2x Blue, 1x Locomotive
        root.setsDrawStack(List(3) { WagonCard(Color.BLUE) })
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(0, 4), playerCards.subList(5, 8), 7, 36
        )
        root.undo()
        //4x Blue, Pay with 3x Locomotive
        root.setsDrawStack(List(3) { WagonCard(Color.BLUE) })
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(0, 4), playerCards.subList(7, 10), 7, 36
        )
        root.undo()
        //2x Blue, 2x Locomotive, Pay with 3x Blue
        root.setsDrawStack(List(2) { WagonCard(Color.BLUE) } + WagonCard(Color.JOKER))
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(5, 9), playerCards.subList(0, 3), 7, 36
        )
        root.undo()
        //2x Blue, 2x Locomotive, Pay with 3x Locomotive
        root.setsDrawStack(List(2) { WagonCard(Color.BLUE) } + WagonCard(Color.JOKER))
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(5, 9), playerCards.subList(9, 12), 7, 36
        )
        root.undo()
        //4x Locomotive, Pay with 3x Blue
        root.setsDrawStack(List(3) { WagonCard(Color.BLUE) } )
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(7, 11), emptyList(),7, 36
        )
        root.undo()
        //4x Locomotive, Pay with 3x Locomotive
        root.setsDrawStack(List(3) { WagonCard(Color.JOKER) } )
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(7, 11), playerCards.subList(11, 14),7, 36
        )
        root.undo()
        //4x Locomotive, Pay with 3x Locomotive
        root.setsDrawStack(List(3) { WagonCard(Color.JOKER) } )
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(7, 11), playerCards.subList(11, 14),7, 36
        )
        root.undo()
        //4x Locomotive, Pay with 2x Locomotive
        root.setsDrawStack(List(2) { WagonCard(Color.JOKER) }  + WagonCard(Color.BLUE) )
        root.playerActionService.assertFailedTunnelClaim(
            route, playerCards.subList(7, 11), playerCards.subList(11, 14)
        )
        root.undo()
        //4x Locomotive, Pay with 3x Blue
        root.setsDrawStack(List(3) { WagonCard(Color.JOKER) } )
        root.playerActionService.assertFailedTunnelClaim(route, playerCards.subList(7, 11), playerCards.subList(0, 3))
        root.undo()
        //4x Locomotive, Pay with 1x Blue
        root.setsDrawStack(List(3) { WagonCard(Color.YELLOW) } )
        root.playerActionService.assertFailedTunnelClaim(route, playerCards.subList(7, 11), playerCards.subList(0, 1))
        root.undo()
        //4x Locomotive, no pay
        root.setsDrawStack(List(3) { WagonCard(Color.YELLOW) } )
        root.playerActionService.assertClaimTunnelSuccess(
            route, playerCards.subList(7, 11), emptyList()  , 7, 36
        )
        root.undo()
    }

    /**
     * Tests the tunnel special cases
     */
    @Test
    fun testTunnelSpecialCases() {
        val playerCards = List(7) { WagonCard(Color.BLUE) } +
                List(7) { WagonCard(Color.JOKER) } +
                List(3) { WagonCard(Color.ORANGE) }
        val state = State(
            emptyList(), emptyList(), listOf(), emptyList(),
            listOf(
                Player(0, "adadsd", emptyList(), playerCards, isRemote = false),
                Player(0, "kfwrjfijfw", emptyList(), emptyList(), isRemote = false)
            ),
            cities = constructGraph()
        )
        val cities = state.cities.associateBy { it.name }
        val root = RootService()
        root.game = Game(state)

        fun RootService.setsDrawStack(newStack: List<WagonCard>) {
            insert(
                game.currentState.copy(
                    wagonCardsStack = newStack
                )
            )
        }

        val route = kotlin.run {
            val route = checkNotNull(cities["Bergen"]).findRoute(checkNotNull(cities["Oslo"]))
            if (route.color == Color.BLUE) {
                route
            } else {
                checkNotNull(route.sibling)
            }
        }
        root.setsDrawStack(listOf(WagonCard(Color.PURPLE)))
        val prevState = root.game.currentState
        root.playerActionService.claimRoute(route, playerCards.subList(0, 4))
        root.playerActionService.afterClaimTunnel(route as Tunnel, null)
        var newState = root.game.currentState
        assertSame(prevState.players,newState.players)
        assert(newState.wagonCardsStack.isEmpty())
        assert(newState.discardStack == listOf(WagonCard(Color.PURPLE)))
        root.undo()
        assertSame(prevState, root.game.currentState)
        root.insert(prevState.copy(discardStack = listOf(WagonCard(Color.BLACK))))
        root.setsDrawStack(listOf(WagonCard(Color.PURPLE), WagonCard(Color.YELLOW)))
        val finalPrevState = root.game.currentState
        root.playerActionService.claimRoute(route, playerCards.subList(0, 4))
        val expectedWagonCard = finalPrevState.discardStack + finalPrevState.wagonCardsStack
        newState = root.game.currentState
        assert(newState.wagonCardsStack.size == expectedWagonCard.size)
        assert(newState.wagonCardsStack.zip(expectedWagonCard).all { it.first === it.second })
        assert(newState.discardStack.size == 4)
        assert(newState.discardStack.zip(playerCards.subList(0, 4)).all { it.first === it.second })
    }

    /**
     * Tests whether [PlayerActionService.validateClaimRoute] works with exhaustive set to false
     */
    @Test
    fun testNonExhaustive() {
        val playerCard = List(4) { WagonCard(Color.ORANGE) } + List(4) { WagonCard(Color.RED) }
        val state = State(
            emptyList(), emptyList(), emptyList(), emptyList(),
            listOf(
                Player(0, "asdasd", emptyList(), playerCard, isRemote = false),
                Player(0, " ufhgiot", emptyList(), emptyList(), isRemote = false)
            ),
            cities = constructGraph()
        )
        val root = RootService()
        root.game = Game(state)
        val cities = state.cities.associateBy { it.name }
        val ore = checkNotNull(cities["Örebro"])
        val sun = checkNotNull(cities["Sundsvall"])
        root.playerActionService.validateClaimRoute(state.currentPlayer, ore.findRoute(sun), playerCard, false)
    }
}