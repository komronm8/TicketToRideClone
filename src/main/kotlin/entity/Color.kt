package entity

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

    fun maptoMessageColor(): service.message.Color{
        return when (this){
            JOKER -> service.message.Color.RAINBOW
            BLUE -> service.message.Color.BLUE
            YELLOW -> service.message.Color.YELLOW
            WHITE -> service.message.Color.WHITE
            BLACK -> service.message.Color.BLACK
            ORANGE -> service.message.Color.ORANGE
            GREEN -> service.message.Color.GREEN
            RED -> service.message.Color.RED
            PURPLE -> service.message.Color.PURPLE
        }
    }
}