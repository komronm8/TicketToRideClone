package view

interface Refreshable {

    fun refreshAfterStartNewGame() {}

    fun refreshAfterNextPlayer() {}

    fun refreshAfterEndGame() {}

    fun refreshAfterDrawDestinationCards() {}

    fun refreshAfterDrawWagonCards() {}

    fun refreshAfterClaimRoute() {}

    fun refreshAfterAfterClaimTunnel() {}

    fun refreshAfterUndoRedo() {}

    fun refreshAfterChooseDestinationCard() { refreshAfterNextPlayer() }
}