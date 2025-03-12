package com.example.tripshare.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tripshare.Models.Place
import com.example.tripshare.R
import com.example.tripshare.activities.PlaceDetailsActivity
import com.google.common.reflect.TypeToken
import com.google.gson.Gson



class FavoritesAdapter(private var places: MutableList<Place>, private val context: Context) :
    RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("Favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewPlace: ImageView = view.findViewById(R.id.imageViewPlace)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        val textViewCategory: TextView = view.findViewById(R.id.textViewCategory)
        val favoriteButton: ImageView = view.findViewById(R.id.favoriteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = places[position]
        holder.textViewTitle.text = place.name
        holder.textViewCategory.text = place.categories.firstOrNull() ?: "No Category"

        Glide.with(holder.itemView.context)
            .load(place.imageUrl ?: R.drawable.placeholder_image)
            .into(holder.imageViewPlace)

        holder.favoriteButton.setImageResource(R.drawable.ic_favorite)

        holder.favoriteButton.setOnClickListener {
            removeFavorite(place, position)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaceDetailsActivity::class.java).apply {
                putExtra("PLACE_ID", place.placeId)
                putExtra("LAT", place.position.latitude)
                putExtra("LON", place.position.longitude)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = places.size

    fun updateData(newPlaces: List<Place>) {
        places.clear()
        places.addAll(newPlaces)

        if (newPlaces.isEmpty()) {
            Log.e("FavoritesAdapter", "⚠️ WARNING: RecyclerView ")
        } else {
            Log.d("FavoritesAdapter", "✅ עדכון נתונים - ${newPlaces.size}  ")
        }

        notifyDataSetChanged()
    }

    private fun removeFavorite(place: Place, position: Int) {
        places.removeAt(position)
        notifyItemRemoved(position)

        val editor = sharedPreferences.edit()
        val savedFavorites = sharedPreferences.getString("favorite_places", "[]") ?: "[]"

        val type = object : TypeToken<MutableList<Place>>() {}.type
        val favoritePlaces: MutableList<Place> = gson.fromJson(savedFavorites, type) ?: mutableListOf()

        favoritePlaces.removeAll { it.placeId == place.placeId }

        editor.putString("favorite_places", gson.toJson(favoritePlaces))
        editor.apply()

        Log.d("FavoritesAdapter", "❌ הוסר מהמועדפים: ${place.name}")
    }

}