package com.example.tripshare.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.tripshare.Models.Place

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FavoritesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)

    private val listeners = mutableListOf<() -> Unit>()

    fun getFavorites(): List<Place> {
        val json = sharedPreferences.getString("favorites_list", "[]") ?: "[]"
        val type = object : TypeToken<List<Place>>() {}.type
        val favorites = Gson().fromJson<List<Place>>(json, type)

        Log.d("FavoritesManager", "📢 getFavorites() returned ${favorites.size} places")

        for (place in favorites) {
            Log.d("FavoritesManager", "⭐ Favorite: ${place.name} - ${place.placeId}")
        }

        return favorites
    }

    fun isFavorite(placeId: String): Boolean {
        return getFavorites().any { it.placeId == placeId }
    }

    fun toggleFavorite(place: Place): Boolean {
        val favoritesList = getFavorites().toMutableList()
        val exists = favoritesList.any { it.placeId == place.placeId }

        if (exists) {
            favoritesList.removeIf { it.placeId == place.placeId }
            Log.d("FavoritesManager", "❌ Removed from favorites: ${place.name}")
        } else {
            favoritesList.add(place)
            Log.d("FavoritesManager", "✅ Added to favorites: ${place.name}")
        }

        saveFavorites(favoritesList)
        notifyListeners()

        return !exists
    }

    private fun saveFavorites(favoritesList: List<Place>) {
        val json = Gson().toJson(favoritesList)
        sharedPreferences.edit().putString("favorites_list", json).apply()

        Log.d(
            "FavoritesManager",
            "💾 Saved ${favoritesList.size} favorite places to SharedPreferences"
        )
    }


    private fun notifyListeners() {
        Log.d("FavoritesManager", "🔄 Notifying all listeners about favorite changes...")
        listeners.forEach { it.invoke() }
    }

}