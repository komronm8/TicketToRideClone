package service.message

/**
 * Enum used to represent all possible TrainCard colors
 * as well as all possible rail colors
 * Used in [GameInitMessage], [DrawTrainCardMessage]
 * and [ClaimARouteMessage]
 */
enum class Color {
    GRAY,
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
    RAINBOW
}