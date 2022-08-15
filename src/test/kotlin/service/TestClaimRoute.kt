package service

import entity.*
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame

class TestClaimRoute {
    private fun PlayerActionService.assertFailedClaim(route: Route, cards: List<WagonCard>) {
        val state = root.game.currentState
        assertFails { claimRoute(route, cards) }
        assertSame(state, root.game.currentState)
    }
    private fun City.findRoute(to: City): Route = checkNotNull(routes.find {
        (it.cities.first === this && it.cities.second === to)
                || (it.cities.first === to && it.cities.second === this)
    })
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
    ) {
        val oldIdx = root.game.currentState.currentPlayerIndex
        val oldCards = root.game.currentState.currentPlayer.wagonCards
        val oldRoutes = root.game.currentState.currentPlayer.claimedRoutes
        val oldDiscard = root.game.currentState.discardStack
        claimRoute(route, usedCards)
        root.game.currentState.players[oldIdx].run {
            assertEquals(newPoints, points)
            assertEquals(newTrainCars, trainCarsAmount)
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

    @Test
    fun testClaimRouteFail() {
        val root = RootService()
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(),  listOf(WagonCard(Color.RED)), 40, emptyList(), false),
                Player(0, "abv", emptyList(),  emptyList(), 40, emptyList(), false)
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
        root.playerActionService.assertFailedClaim(
            hel.findRoute(ima),
            List(3) { state1.players.first().wagonCards.first() }
        )
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), List(3) { WagonCard(Color.RED) })
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), List(3) { WagonCard(Color.RED) })
        root.playerActionService.assertFailedClaim(hel.findRoute(lah), List(3) { WagonCard(Color.BLACK) })
        val newPlayerCards =
            listOf(WagonCard(Color.JOKER)) +
                    List(4) { WagonCard(Color.RED) } +
                    List(3) { WagonCard(Color.BLUE) } +
                    List(3) { WagonCard(Color.JOKER) }
        val state2 = state1.updatedPlayer {
            copy(wagonCards =  newPlayerCards)
        }
        root.insert(state2)
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
        root.playerActionService.assertFailedClaim(oul.findRoute(kuo), newPlayerCards.subList(3,6))
        //1x Locomotive, 2x Red
        root.playerActionService.assertFailedClaim(oul.findRoute(kuo), newPlayerCards.subList(0,3))
        //2x Locomotive, 3x Blue
        root.playerActionService.assertFailedClaim(oul.findRoute(kuo), newPlayerCards.subList(4,9))
        val state3 = state2.updatedPlayer(1) { copy(claimedRoutes = listOf(hel.findRoute(ima))) }
        root.insert(state3)
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(1, 3))
        val state4 = state2.updatedPlayer { copy(claimedRoutes = listOf(hel.findRoute(ima))) }
        root.insert(state4)
        root.playerActionService.assertFailedClaim(hel.findRoute(ima), newPlayerCards.subList(1, 3))
    }

    @Test
    fun testClaimFerryFail() {
        val root = RootService()
        val cards = List(4) { WagonCard(Color.GREEN) }
            .plus(List(2) {  WagonCard(Color.JOKER) })
            .plus(List(2) { WagonCard(Color.PURPLE) })
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(),
                    wagonCards = cards,
                    40, emptyList(), false)
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

    @Test
    fun testInsufficientTrainCars() {
        val root = RootService()
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(),  List(3) { WagonCard(Color.RED) }, 2, emptyList(), false)
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

    @Test
    fun testDoubleRoute() {
        val root = RootService()
        val state1 = State(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            listOf(
                Player(0, "abc", emptyList(),  List(3) { WagonCard(Color.RED) }, 40, emptyList(), false),
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
                assertEquals(37, trainCarsAmount)
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
            players = state2.players + Player(0, "jk", emptyList(), emptyList(), 2,  emptyList(), false)
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
        val mur  = checkNotNull(cities["Murmansk"])
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

    fun testTunnel() {
        val playerCards = List(6) { WagonCard(Color.BLUE) } + List(6) { WagonCard(Color.JOKER) }
        val state = State(
            emptyList(), emptyList(), listOf(), emptyList(),
            listOf(),
            cities = constructGraph()
        )
        val cities = state.cities.associateBy { it.name }
        val root = RootService()
        root.game = Game(state)
        val route = kotlin.run {
            val route = checkNotNull(cities["Bergen"]).findRoute(checkNotNull(cities["Oslo"]))
            if (route.color == Color.BLUE) {
                route
            } else {
                checkNotNull(route.sibling)
            }
        }

        // 4x Blue
        root.playerActionService.assertClaimRouteSuccess(route, playerCards.subList(0, 4), 7, 36)
        root.undo()

    }
}