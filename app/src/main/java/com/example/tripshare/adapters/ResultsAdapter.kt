package com.example.tripshare.adapters


import android.content.Context
import android.content.Intent
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
import com.example.tripshare.utils.FavoritesManager




class ResultsAdapter(
    private var places: List<Place>,
    private val context: Context,
    private val onFavoriteToggle: () -> Unit
) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    private val favoritesManager = FavoritesManager(context)

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

        val isFavorite = favoritesManager.isFavorite(place.placeId)
        holder.favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )

        holder.favoriteButton.setOnClickListener {
            val newFavoriteState = favoritesManager.toggleFavorite(place)
            holder.favoriteButton.setImageResource(
                if (newFavoriteState) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )
            onFavoriteToggle()
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
        Log.d("ResultsAdapter", "🔄 Called updateData with ${newPlaces.size} places")

        if (newPlaces.isEmpty()) {
            Log.e("ResultsAdapter", "⚠️ WARNING: RecyclerView received an empty list!")
            return
        }

        val updatedList = places.toMutableList()
        newPlaces.forEach { newPlace ->
            val index = updatedList.indexOfFirst { it.placeId == newPlace.placeId }
            if (index != -1) {
                updatedList[index] = newPlace
            } else {
                updatedList.add(newPlace)
            }
        }

        places = updatedList
        notifyDataSetChanged()
    }
}

