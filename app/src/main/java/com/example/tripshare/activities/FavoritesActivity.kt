package com.example.tripshare.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tripshare.Models.Place
import com.example.tripshare.R
import com.example.tripshare.adapters.FavoritesAdapter
import com.example.tripshare.utils.FavoritesManager
import com.google.android.material.bottomnavigation.BottomNavigationView


class FavoritesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var favoritesManager: FavoritesManager
    private var favoritePlaces = mutableListOf<Place>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        favoritesManager = FavoritesManager(this)
        recyclerView = findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadFavorites()

        favoritesAdapter = FavoritesAdapter(favoritePlaces,this)
        recyclerView.adapter = favoritesAdapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNavigationView.selectedItemId = R.id.navigation_favourites

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, PlanTripActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    finish()
                    true
                }
                R.id.navigation_favourites -> true
                else -> false
            }
        }

    }

    private fun loadFavorites() {
        favoritePlaces.clear()
        favoritePlaces.addAll(favoritesManager.getFavorites())

        Log.d("FavoritesActivity", "📢 Loaded ${favoritePlaces.size} favorite places")

        if (::favoritesAdapter.isInitialized) {
            favoritesAdapter.updateData(favoritePlaces)
        }

        recyclerView.visibility = if (favoritePlaces.isNotEmpty()) View.VISIBLE else View.GONE
    }
}