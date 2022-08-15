package entity

/**
 * The game containing a series a states
 */
class Game(state: State) {
    val states: ArrayList<State> = arrayListOf(state)
    var gameState: GameState = GameState.DEFAULT
    var currentStateIndex: Int = 0
        set(index) {
            assert(index in states.indices)
            field = index
        }

    /**
     * The current game state
     */
    val currentState: State
        get() = states[currentStateIndex]
}