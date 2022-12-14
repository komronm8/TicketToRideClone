package view

import entity.Player
import entity.Route
import entity.WagonCard

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
    fun refreshAfterEndGame(winner: Player) {}

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
    fun refreshAfterClaimRoute(route: Route, cardsUsed: List<WagonCard>) {}

    /**
     * Gets called after [service.PlayerActionService.afterClaimTunnel]
     */
    fun refreshAfterAfterClaimTunnel(route: Route) {}

    /**
     * Gets called after [service.PlayerActionService.undo] and [service.PlayerActionService.redo]
     */
    fun refreshAfterUndoRedo() {}

    /**
     * Gets called after [service.GameService.chooseDestinationCard]
     */
    fun refreshAfterChooseDestinationCard() { refreshAfterNextPlayer() }

    /**
     * Gets called after [service.GameService.chooseDestinationCards]
     */
    fun refreshAfterOneDestinationCard() {}

    /**
     * Gets called after [service.NetworkService.client.onPlayerNotification]
     */
    fun refreshAfterPlayerJoin() {}

    /**
     * Gets called after [service.NetworkService.client.onPlayerNotification]
     */
    fun refreshAfterPlayerDisconnect() {}

    /**
     * Gets called after an eroor accourt
     */
    fun refreshAfterError(error: String){}

    /**
     * Gets called after chat message
     */
    fun refreshAfterText(text: String) {}
}