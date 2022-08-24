package entity

import service.network.message.Color

/**
 * The Color of a [WagonCard]/[Route]. [JOKER] stands for the color gray in the context of [Routes][Route]
 * and for a locomotive card in the context of [WagonCards][WagonCard]
 */
enum class Color {
    GREEN,
    ORANGE,
    YELLOW,
    RED,
    PURPLE,
    BLUE,
    WHITE,
    BLACK,
    JOKER;

    fun maptoMessageColor(): Color {
        return when (this){
            JOKER -> Color.RAINBOW
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