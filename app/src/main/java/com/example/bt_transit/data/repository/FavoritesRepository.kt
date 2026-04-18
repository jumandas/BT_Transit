package com.example.bt_transit.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "bt_favorites"
private const val KEY_FAV_ROUTES = "fav_routes"
private const val MAX_FAVORITES = 3

@Singleton
class FavoritesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favorites = MutableStateFlow(loadFromPrefs())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private fun loadFromPrefs(): Set<String> =
        prefs.getStringSet(KEY_FAV_ROUTES, emptySet()) ?: emptySet()

    fun isFavorite(routeId: String): Boolean = _favorites.value.contains(routeId)

    /** Returns false if already at max capacity and routeId is not already a favorite. */
    fun toggleFavorite(routeId: String): Boolean {
        val current = _favorites.value.toMutableSet()
        return if (current.contains(routeId)) {
            current.remove(routeId)
            save(current)
            true
        } else if (current.size < MAX_FAVORITES) {
            current.add(routeId)
            save(current)
            true
        } else {
            false // at capacity
        }
    }

    private fun save(set: Set<String>) {
        prefs.edit().putStringSet(KEY_FAV_ROUTES, set).apply()
        _favorites.value = set.toSet()
    }
}
