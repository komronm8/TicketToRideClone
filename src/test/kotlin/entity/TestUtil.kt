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