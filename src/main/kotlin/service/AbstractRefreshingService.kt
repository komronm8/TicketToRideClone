package service

import view.Refreshable

/**
 * A base service for all observable services
 */
abstract class AbstractRefreshingService {

    private val refreshables = mutableListOf<Refreshable>()

    /**
     * Fügt ein [Refreshable] dem Service hinzu
     *
     * @param newRefreshable das [Refreshable] zum Hinzufügen
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables += newRefreshable
    }

    /**
     * Führt die Methode auf allen [refreshables] aus
     *
     * @param method Die Methode zum Ausführen
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) =
        refreshables.forEach { it.method() }

    }