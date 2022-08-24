package service.network.message

/**
 * Enum representing the different colors a player may have,
 * used in [Player] which is used in [GameInitMessage]
 */
enum class PlayerColor {
    RED,
    WHITE,
    PURPLE;

    fun maptoGameColor(): entity.Color {
        return when (this){
            RED -> entity.Color.RED
            WHITE -> entity.Color.WHITE
            PURPLE -> entity.Color.PURPLE
        }

    }
}