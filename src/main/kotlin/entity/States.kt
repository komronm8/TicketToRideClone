package entity

class States(game: Game) {
    private val states: ArrayList<Game> = arrayListOf(game)

    var currentStateIndex: Int = 0
        private set(index) {
            assert(index in states.indices)
            field = index
        }
    val currentState: Game
        get() = states[currentStateIndex]

    fun undo() {
        currentStateIndex = if (currentStateIndex > 0) currentStateIndex - 1 else 0
    }
    fun redo() {
        currentStateIndex = if (currentStateIndex < states.size - 1) currentStateIndex + 1 else currentStateIndex
    }
    fun insert(game: Game) {
        //the range is reversed in order to delete from the back since this is more efficient
        for (index in (currentStateIndex + 1 until states.size).reversed()) {
            states.removeAt(index)
        }
        states.add(game)
        currentStateIndex += 1
    }
}