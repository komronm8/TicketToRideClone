package entity

import kotlin.test.Test
import tools.aqua.bgw.util.Stack
import kotlin.test.assertEquals

/**
 * Tests the [States] class
 */
class TestStates {
    private fun game() = Game(Stack(), emptyList(), Stack(), Stack(), emptyList(), cities = emptyList())

    /**
     * Tests [States.insert], [States.undo] and [States.redo]
     */
    @Test
    fun testStatesProcedure() {
        val game = game()
        val game2 = game.copy(currentPlayer = 1)
        val game3 = game.copy(currentPlayer = 2)
        val game4 = game.copy(currentPlayer = 3)
        val states = States(game)
        states.insert(game2)
        assertEquals(game2, states.currentState)
        states.insert(game3)
        assertEquals(game3, states.currentState)
        states.undo()
        assertEquals(game2, states.currentState)
        states.undo()
        assertEquals(game, states.currentState)
        states.undo()
        assertEquals(game, states.currentState)
        states.redo()
        assertEquals(game2, states.currentState)
        states.redo()
        assertEquals(game3, states.currentState)
        states.undo()
        assertEquals(game2, states.currentState)
        states.undo()
        assertEquals(game, states.currentState)
        states.insert(game4)
        assertEquals(game4, states.currentState)
        states.redo()
        assertEquals(game4, states.currentState)
        states.undo()
        assertEquals(game, states.currentState)
    }
}