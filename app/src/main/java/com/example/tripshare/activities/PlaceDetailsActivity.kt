package com.example.tripshare.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.tripshare.R

class PlaceDetailsActivity : AppCompatActivity() {

    private lateinit var placeName: TextView
    private lateinit var placeCategory: TextView
    private lateinit var placeAddress: TextView
    private lateinit var placeRating: TextView
    private lateinit var placeImage: ImageView
    private lateinit var btnNavigate: Button

    private val API_KEY = "AIzaSyBgxOG6XZxQCnxFlV3PAhcCIopofKQ0vCY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_details)

        // ✅ הגדרת חץ חזרה בפעולה
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 🏠 אתחול רכיבי UI
        placeName = findViewById(R.id.placeName)
        placeCategory = findViewById(R.id.placeCategory)
        placeAddress = findViewById(R.id.placeAddress)
        placeRating = findViewById(R.id.placeRating)
        placeImage = findViewById(R.id.placeImage)
        btnNavigate = findViewById(R.id.btnNavigate)

        // 📥 קבלת נתונים מה-Intent
        val placeId = intent.getStringExtra("PLACE_ID")
        val lat = intent.getDoubleExtra("LAT", 0.0)
        val lon = intent.getDoubleExtra("LON", 0.0)

        if (!placeId.isNullOrEmpty()) {
            fetchPlaceDetails(placeId)
        } else {
            Log.e("PlaceDetails", "❌ PLACE_ID is missing")
        }

        // 🗺️ ניווט לגוגל מפות
        btnNavigate.setOnClickListener {
            val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=name,rating,formatted_address,photos,types&key=$API_KEY"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val result = response.optJSONObject("result") ?: return@JsonObjectRequest

                val name = result.optString("name", "Unknown Place")
                val address = result.optString("formatted_address", "Address not available")
                val rating = result.optDouble("rating", 0.0)
                val categories = result.optJSONArray("types")?.let { jsonArray ->
                    List(jsonArray.length()) { jsonArray.optString(it) }
                } ?: emptyList()

                val photoReference = result.optJSONArray("photos")?.getJSONObject(0)?.optString("photo_reference", null)
                val imageUrl = if (!photoReference.isNullOrEmpty()) {
                    "https://maps.googleapis.com/maps/api/place/photo?maxwidth=600&photoreference=$photoReference&key=$API_KEY"
                } else {
                    null
                }

                // 🏷️ עדכון UI
                placeName.text = name
                placeAddress.text = address
                placeRating.text = "⭐ $rating"
                placeCategory.text = categories.joinToString(", ")

                // 🖼️ טעינת תמונה (אם קיימת)
                Glide.with(this)
                    .load(imageUrl ?: R.drawable.placeholder_image) // אם אין תמונה, מציג תמונת ברירת מחדל
                    .into(placeImage)
            },
            { error ->
                Log.e("API", "❌ Error fetching place details: ${error.message}")
            })

        Volley.newRequestQueue(this).add(request)
    }

    // 🎯 חץ חזרה
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish() // סגירת האקטיביטי וחזרה אחורה
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
