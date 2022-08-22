package service.ai

import entity.Route
import entity.WagonCard

/**
 * A move that can be performed with (mostly) deterministic effec
 */
sealed interface AIMove {
    /**
     * An action corresponding to [service.PlayerActionService.drawWagonCard]
     */
    data class DrawWagonCard(val firstDraw: Int, val secondDraw: Int) : AIMove
    /**
     * An action corresponding to [service.PlayerActionService.drawDestinationCards]
     */
    data class DrawDestinationCard(val destinationCards: List<Int>) : AIMove
    /**
     * An action corresponding to [service.PlayerActionService.claimRoute]
     */
    data class ClaimRoute(val route: Route, val usedCards: List<WagonCard>, val tunnelCards: List<WagonCard>?) :
        AIMove
}