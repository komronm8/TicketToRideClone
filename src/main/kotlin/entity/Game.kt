package entity

/**
 * The game containing a series a states
 */
class Game(state: State) {
    private val states: ArrayList<State> = arrayListOf(state)
    var gameState: GameState = GameState.DEFAULT
    private var currentStateIndex: Int = 0
        set(index) {
            assert(index in states.indices)
            field = index
        }

    /**
     * The current game state
     */
    val currentState: State
        get() = states[currentStateIndex]

    /**
     * Reverts the game state to the round before.
     */
    fun undo() {
        currentStateIndex = if (currentStateIndex > 0) currentStateIndex - 1 else 0
    }

    /**
     * Recovers game states undone by [undo]
     */
    fun redo() {
        currentStateIndex = if (currentStateIndex < states.size - 1) currentStateIndex + 1 else currentStateIndex
    }

    /**
     * Inserts the new game state as the current game state.
     * Any record of [undone][undo] game states will be destroyed and won't be recoverable by [redo]
     *
     * @param state The game to insert
     */
    fun insert(state: State) {
        //the range is reversed in order to delete from the back since this is more efficient
        for (index in (currentStateIndex + 1 until states.size).reversed()) {
            states.removeAt(index)
        }
        states.add(state)
        currentStateIndex += 1
    }
}