package entity

/**
 * The states a game assumed
 */
class States(game: Game) {
    private val states: ArrayList<Game> = arrayListOf(game)
    private var currentStateIndex: Int = 0
        set(index) {
            assert(index in states.indices)
            field = index
        }

    /**
     * The current game state
     */
    val currentState: Game
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
     * @param game The game to insert
     */
    fun insert(game: Game) {
        //the range is reversed in order to delete from the back since this is more efficient
        for (index in (currentStateIndex + 1 until states.size).reversed()) {
            states.removeAt(index)
        }
        states.add(game)
        currentStateIndex += 1
    }
}