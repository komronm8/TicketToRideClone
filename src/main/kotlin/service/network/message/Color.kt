package service.network.message

import entity.Color

/**
 * Enum used to represent all possible TrainCard colors
 * as well as all possible rail colors
 * Used in [GameInitMessage], [DrawTrainCardMessage]
 * and [ClaimARouteMessage]
 */
enum class Color {
    BLUE,
    YELLOW,
    WHITE,
    BLACK,
    ORANGE,
    GREEN,
    RED,
    PURPLE,

    /**
     * In TrainCard context used as LOCOMOTIVE,
     * in rail context used as GREY
     */
    RAINBOW;

    /**
     * Maps message to color
     */
    fun maptoGameColor(): Color{
        return when (this){
            RAINBOW -> Color.JOKER
            BLUE -> Color.BLUE
            YELLOW -> Color.YELLOW
            WHITE -> Color.WHITE
            BLACK -> Color.BLACK
            ORANGE -> Color.ORANGE
            GREEN -> Color.GREEN
            RED -> Color.RED
            PURPLE -> Color.PURPLE
        }
    }
}