package entity

/**
 * The state of the current game round.
 */
sealed interface GameState {
    /**
     * The Default state of the round; no complex interaction
     * has been used
     */
    object DEFAULT: GameState

    /**
     * The game state after the player drew one wagon card,
     * and has yet to draw a second one
     */
    data class DREW_WAGON_CARD(val shuffled: List<WagonCard>? = null): GameState

    /**
     * The game state after the player drew one card
     * The used cards are pushed onto the discard stack,
     * while the required cards are on top of the draw stack
     */
    object AFTER_CLAIM_TUNNEL: GameState

    object CHOOSE_DESTINATION_CARD: GameState
}