package service

import entity.Game
import entity.State
import service.network.NetworkService
import view.Refreshable

/**
 * Rootservice to communicate with entity layer
 */
class RootService {
    lateinit var game: Game
    val playerActionService: PlayerActionService = PlayerActionService(this)
    val gameService: GameService = GameService(this)
    val network: NetworkService = NetworkService(this)

    /**
     * Reverts the game state to the round before.
     */
    fun undo() {
        game.currentStateIndex = if (game.currentStateIndex > 0) game.currentStateIndex - 1 else 0
    }

    /**
     * Recovers game states undone by [undo]
     */
    fun redo() {
        game.apply {
            currentStateIndex = if (currentStateIndex < states.size - 1) currentStateIndex + 1 else currentStateIndex
        }
    }

    /**
     * Inserts the new game state as the current game state.
     * Any record of [undone][undo] game states will be destroyed and won't be recoverable by [redo]
     *
     * @param state The game to insert
     */
    fun insert(state: State) {
        //the range is reversed in order to delete from the back since this is more efficient
        for (index in (game.currentStateIndex + 1 until game.states.size).reversed()) {
            game.states.removeAt(index)
        }
        game.states.add(state)
        game.currentStateIndex += 1
    }

    /**
     * Adds a refreshable to [gameService] and to [playerActionService]
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        network.addRefreshable(newRefreshable)
    }

    /**
     * Calls [addRefreshable] with all given [newRefreshables]
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach(this::addRefreshable)
    }
}