package service

/**
 * Enum to distinguish the different states that occur in networked games
 */
enum class ConnectionState {
    /**
     * No connection currently active, first state the program will
     * go into after start or after an active connection was closed
     */
    DISCONNECTED,

    /**
     * Successfully connected to server, but no game started/joined yet
     */
    CONNECTED,

    /**
     * Error state where clients will go into if anything goes wrong
     * e.g. data is not correctly synchronized / game states are
     * out of sync
     */
    ERROR,

    /**
     * hostGame request sent to server, waiting for confirmation
     */
    WAIT_FOR_HOST_CONFIRMATION,

    /**
     * joinGame request sent to server, waiting for confirmation
     */
    WAIT_FOR_JOIN_CONFIRMATION,

    /**
     * Host game started, waiting for guest players to join
     */
    WAIT_FOR_PLAYERS,

    /**
     * Joined game as guest, waiting for GameInit message
     */
    WAIT_FOR_GAMEINIT,

    /**
     * Game is running, GameInit message was sent and received,
     * state where this client can select destination cards
     * to keep from the five destination cards in GameInit
     */
    BUILD_GAMEINIT_RESPONSE,

    /**
     * Game is running, GameInit message was sent and received,
     * state where this client waits for other players to select
     * the destination cards from GameInit they want to keep
     */
    WAIT_FOR_GAMEINIT_RESPONSE,

    /**
     * Game is running, state where this client waits because
     * it's another player's turn
     */
    WAIT_FOR_TURN,

    /**
     * Game is running, state where it's currently this
     * client's turn
     */
    PLAY_TURN,
}