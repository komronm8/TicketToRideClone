package entity.service

import entity.Game
import entity.State
import service.RootService
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the [Game] class
 */
class TestRoot {
    private fun game() = State(
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        cities = emptyList(),
    )

    /**
     * Tests [RootService.insert], [RootService.undo] and [RootService.redo]
     */
    @Test
    fun testStatesProcedure() {
        val game = game()
        val game2 = game.copy(currentPlayerIndex = 1)
        val game3 = game.copy(currentPlayerIndex = 2)
        val game4 = game.copy(currentPlayerIndex = 3)
        val root = RootService()
        root.game = Game(game)
        root.insert(game2)
        assertEquals(game2, root.game.currentState)
        root.insert(game3)
        assertEquals(game3, root.game.currentState)
        root.undo()
        assertEquals(game2, root.game.currentState)
        root.undo()
        assertEquals(game, root.game.currentState)
        root.undo()
        assertEquals(game, root.game.currentState)
        root.redo()
        assertEquals(game2, root.game.currentState)
        root.redo()
        assertEquals(game3, root.game.currentState)
        root.undo()
        assertEquals(game2, root.game.currentState)
        root.undo()
        assertEquals(game, root.game.currentState)
        root.insert(game4)
        assertEquals(game4, root.game.currentState)
        root.redo()
        assertEquals(game4, root.game.currentState)
        root.undo()
        assertEquals(game, root.game.currentState)
    }
}