package entity

/**
 * The state of the current game round.
 */
enum class GameState {
    /**
     * The Default state of the round; no complex interaction
     * has been used
     */
    DEFAULT,

    /**
     * The game state after the player drew one wagon card,
     * and has yet to draw a second one
     */
    DREW_WAGON_CARD,

    /**
     * The game state after the player drew one card
     * The used cards are pushed onto the discard stack,
     * while the required cards are on top of the draw stack
     */
    AFTER_CLAIM_TUNNEL,

    CHOOSE_DESTINATION_CARD
}