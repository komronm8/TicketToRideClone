package view

/**
 * An observer notifying the view of changes performed by the service layer
 */
interface Refreshable {
    /**
     * Gets called after [service.GameService.startNewGame]
     */
    fun refreshAfterStartNewGame() {}

    /**
     * Gets called after [service.GameService.nextPlayer]
     */
    fun refreshAfterNextPlayer() {}

    /**
     * Gets called after [service.GameService.endGame]
     */
    fun refreshAfterEndGame() {}

    /**
     * Gets called after [service.PlayerActionService.drawDestinationCards]
     */
    fun refreshAfterDrawDestinationCards() {}

    /**
     * Gets called after [service.PlayerActionService.drawWagonCard]
     */
    fun refreshAfterDrawWagonCards() {}

    /**
     * Gets called after [service.PlayerActionService.claimRoute]
     */
    fun refreshAfterClaimRoute() {}

    /**
     * Gets called after [service.PlayerActionService.afterClaimTunnel]
     */
    fun refreshAfterAfterClaimTunnel() {}

    /**
     * Gets called after [service.PlayerActionService.undo] and [service.PlayerActionService.redo]
     */
    fun refreshAfterUndoRedo() {}

    /**
     * Gets called after [service.GameService.chooseDestinationCard]
     */
    fun refreshAfterChooseDestinationCard() { refreshAfterNextPlayer() }
}