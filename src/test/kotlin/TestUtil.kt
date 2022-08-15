package entity

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests that [a] and [b], and their hashes, are equal
 */
fun <T> testEqualsHash(a: T, b: T) {
    assertEquals(a, b)
    assertEquals(a.hashCode(), b.hashCode())
}

/**
 * Tests that [a] and [b], and their hashes, are not equal
 */
fun <T> testNotEqualsHash(a: T, b: T) {
    assertNotEquals(a, b)
    assertNotEquals(a.hashCode(), b.hashCode())
}

/**
 * Creates an updated player list, where the player at [index] is replaced by an updated version,
 * computed by [update]
 *
 * @param update the updater, receives the player at index [index] and returns the new current player
 * @param index the index of the player, that should be updated
 */
fun State.updatePlayer(index: Int, update: Player.()->Player): List<Player> =
    players.toMutableList().also {
        it[index] = it[index].update()
    }
/**
 * Creates an updated state, where the current player is replaced by an updated version,
 * computed by [update]
 *
 * @param update the updater, receives the current player and returns the new current player
 */
fun State.updatedPlayer(update: Player.() -> Player): State =
    updatedPlayer(currentPlayerIndex, update)

/**
 * Creates an updated state, where the player at [index] is replaced by an updated version,
 * computed by [update]
 *
 * @param update the updater, receives the player at index [index] and returns the new current player
 * @param index the index of the player, that should be updated
 */
fun State.updatedPlayer(index: Int, update: Player.() -> Player): State =
    copy(players = updatePlayer(index, update))