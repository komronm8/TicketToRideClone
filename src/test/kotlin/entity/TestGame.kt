package entity

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Game] class
 */
class TestGame {
    private fun game() = State(
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        cities = emptyList(),
    )

    /**
     * Tests [Game.insert], [Game.undo] and [Game.redo]
     */
    @Test
    fun testStatesProcedure() {
        val game = game()
        val game2 = game.copy(currentPlayerIndex = 1)
        val game3 = game.copy(currentPlayerIndex = 2)
        val game4 = game.copy(currentPlayerIndex = 3)
        val states = Game(game)
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