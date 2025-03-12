package com.example.tripshare.activities


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Locale
import android.view.View
import com.example.tripshare.R
import com.example.tripshare.adapters.ResultsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.content.Context
import com.example.tripshare.Models.Place
import com.example.tripshare.utils.FavoritesManager


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class PlanTripActivity() : AppCompatActivity(), OnMapReadyCallback, Parcelable {

    private lateinit var mMap: GoogleMap
    private val markersList = mutableListOf<Marker>()
    private lateinit var autoCompleteLocation: AutoCompleteTextView
    private lateinit var autoCompleteInterests: AutoCompleteTextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSearch: Button
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultsAdapter: ResultsAdapter
    private val placesList = mutableListOf<Place>()
    private val selectedInterests = mutableListOf<String>()
    private lateinit var favoritesManager: FavoritesManager
    private val selectedRoutePlaces = mutableListOf<Place>()

    private val API_KEY = "AIzaSyBgxOG6XZxQCnxFlV3PAhcCIopofKQ0vCY"

    private val allCategories = listOf(
        "Restaurants" to "restaurant",
        "Cafes" to "cafe",
        "Museums" to "museum",
        "Parks" to "park",
        "Shopping Malls" to "shopping_mall",
        "Beaches" to "beach",
        "Historical Sites" to "tourist_attraction",
        "Hiking Trails" to "hiking_trail",
        "Nightlife" to "night_club" )

    constructor(parcel: Parcel) : this() {
        selectedLatitude = parcel.readValue(Double::class.java.classLoader) as? Double
        selectedLongitude = parcel.readValue(Double::class.java.classLoader) as? Double
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_trip)

        favoritesManager = FavoritesManager(this)


        autoCompleteLocation = findViewById(R.id.autoCompleteLocation)
        autoCompleteInterests = findViewById(R.id.autoCompleteInterests)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSearch = findViewById(R.id.btnSearch)
        recyclerView = findViewById(R.id.recyclerViewResults)

        placesList.clear()


        resultsAdapter = ResultsAdapter(placesList, this) {
            resultsAdapter.updateData(placesList) // עדכון המועדפים ברשימה
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = resultsAdapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)

        setupLocationSearch()
        setupInterestSelection()
        setupDateSelection()


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSearch.setOnClickListener {
            if (selectedInterests.isNotEmpty() && selectedLatitude != null && selectedLongitude != null) {
                fetchPlacesFromGoogle(selectedLatitude!!, selectedLongitude!!)
            } else {
                Toast.makeText(this, "Select a location and at least one interest!", Toast.LENGTH_SHORT).show()
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    setResult(RESULT_OK)
                    finish() // חזרה למסך הראשי
                    true
                }
                R.id.navigation_favourites -> {
                    val intent = Intent(this, FavoritesActivity::class.java)
                    startActivityForResult(intent, 1) // מחכים לתוצאה מ-FavoritesActivity
                    true
                }
                else -> false
            }
        }

    }



    private fun setupInterestSelection() {
        val categoryNames = allCategories.map { it.first }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
        autoCompleteInterests.setAdapter(adapter)

        autoCompleteInterests.setOnItemClickListener { _, view, _, _ ->
            val selectedInterestName = (view as TextView).text.toString()
            val selectedInterestApiValue = allCategories.find { it.first == selectedInterestName }?.second

            if (!selectedInterestApiValue.isNullOrEmpty() && !selectedInterests.contains(selectedInterestApiValue)) {
                selectedInterests.add(selectedInterestApiValue)
                Log.d("DEBUG", "✅ Added Interest: $selectedInterestApiValue")
                updateSelectedInterestsUI()
            }

            autoCompleteInterests.text.clear()
        }
    }
    private fun updateSelectedInterestsUI() {
        val selectedInterestNames = selectedInterests.mapNotNull { apiValue ->
            allCategories.find { it.second == apiValue }?.first
        }

        val newText = selectedInterestNames.joinToString(", ")
        Log.d("DEBUG", "🎯 Selected Interests for UI: $newText")
        autoCompleteInterests.hint = newText
    }


    private fun setupLocationSearch() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteLocation.setAdapter(adapter)

        autoCompleteLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length > 2) {
                    fetchLocationsFromGoogle(s.toString(), adapter)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        autoCompleteLocation.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = adapter.getItem(position)
            if (!selectedCity.isNullOrEmpty()) {
                autoCompleteLocation.setText(selectedCity, false)
                CoroutineScope(Dispatchers.IO).launch {
                    val coordinates = fetchCityCoordinates(selectedCity)
                    if (coordinates != null) {
                        withContext(Dispatchers.Main) {
                            selectedLatitude = coordinates.first
                            selectedLongitude = coordinates.second
                        }
                    }
                }
            }
        }
    }


    suspend fun fetchCityCoordinates(city: String): Pair<Double, Double>? = suspendCancellableCoroutine { continuation ->
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$city&key=$API_KEY"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val results = response.optJSONArray("results") ?: return@JsonObjectRequest
                if (results.length() == 0) {
                    continuation.resume(null)
                    return@JsonObjectRequest
                }
                val location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                continuation.resume(Pair(location.getDouble("lat"), location.getDouble("lng")))
            },
            { error -> continuation.resumeWithException(error) }
        )

        Volley.newRequestQueue(applicationContext).add(request)
    }

    private fun fetchLocationsFromGoogle(query: String, adapter: ArrayAdapter<String>) {
        val url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=$query&key=$API_KEY"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val suggestions = mutableListOf<String>()
                response.optJSONArray("predictions")?.let { results ->
                    for (i in 0 until results.length()) {
                        results.getJSONObject(i).optString("description")?.let { suggestions.add(it) }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    adapter.clear()
                    adapter.addAll(suggestions)
                    adapter.notifyDataSetChanged()
                    if (suggestions.isNotEmpty()) autoCompleteLocation.showDropDown()
                }
            },
            { error -> Log.e("API", "Error fetching locations: ${error.message}") }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun setupDateSelection() {
        val cardDate = findViewById<MaterialCardView>(R.id.cardDate)
        cardDate.setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()

            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Dates")
                .setCalendarConstraints(constraints)
                .build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selection.first)
                val endDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selection.second)
                tvSelectedDate.text = "$startDate - $endDate"
                tvSelectedDate.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(selectedLatitude)
        parcel.writeValue(selectedLongitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlanTripActivity> {
        override fun createFromParcel(parcel: Parcel): PlanTripActivity {
            return PlanTripActivity(parcel)
        }

        override fun newArray(size: Int): Array<PlanTripActivity?> {
            return arrayOfNulls(size)
        }
    }

    private fun fetchPlacesFromGoogle(lat: Double, lon: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val typesParam = selectedInterests.joinToString("|")
            val url =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$lon&radius=5000&type=$typesParam&key=$API_KEY"

            Log.d("DEBUG", "🌍 Fetching places from API: $url")

            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    val fetchedPlaces = mutableListOf<Place>()
                    val results = response.optJSONArray("results") ?: return@JsonObjectRequest

                    for (i in 0 until results.length()) {
                        val placeJson = results.getJSONObject(i)
                        val placeId = placeJson.optString("place_id", "")
                        val categories = placeJson.optJSONArray("types")?.let { jsonArray ->
                            List(jsonArray.length()) { jsonArray.optString(it) }
                        } ?: emptyList()

                        val name = placeJson.optString("name", "Unknown Place")
                        Log.d("DEBUG", "✅ Found Place: $name")

                        val location = placeJson.getJSONObject("geometry").getJSONObject("location")
                        val position = LatLng(location.getDouble("lat"), location.getDouble("lng"))

                        val photoReference = placeJson.optJSONArray("photos")?.getJSONObject(0)
                            ?.optString("photo_reference")
                        val imageUrl =
                            photoReference?.let { "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=$it&key=$API_KEY" }
                                ?: ""
                        val isFavorite = favoritesManager.isFavorite(placeId)

                        fetchedPlaces.add(Place(placeId, name, categories, imageUrl, position))
                    }

                    Log.d("DEBUG", "📢 New places received: ${fetchedPlaces.size}")

                    runOnUiThread {
                        if (fetchedPlaces.isNotEmpty()) {
                            recyclerView.visibility = View.VISIBLE
                            resultsAdapter.updateData(fetchedPlaces)
                            // ✅ הוספת המקומות למפה!
                            addPlacesToMap(fetchedPlaces)

                        } else {
                            recyclerView.visibility = View.GONE
                            Log.e("DEBUG", "⚠️ No places found - RecyclerView update skipped!")
                        }
                    }
                },
                { error ->
                    Log.e(
                        "ERROR",
                        "❌ API Error: ${error.networkResponse?.statusCode} ${error.message}"
                    )
                }
            )

            Volley.newRequestQueue(this@PlanTripActivity).add(request)
        }
    }


    private fun addPlacesToMap(places: List<Place>) {
        markersList.forEach { it.remove() }
        markersList.clear()

        places.forEach { place ->
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(place.position)
                    .title(place.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            marker?.let { markersList.add(it) }
        }

        if (places.isNotEmpty()) {
            val firstPlace = places.first().position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPlace, 12f))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // אם יש כבר מיקום נבחר, נתמקד בו
        selectedLatitude?.let { lat ->
            selectedLongitude?.let { lon ->
                val location = LatLng(lat, lon)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            updateFavoritesStatus()
        }
    }

    private fun updateFavoritesStatus() {
        placesList.forEach { place ->
            place.isFavorite = favoritesManager.isFavorite(place.placeId)
        }
        resultsAdapter.updateData(placesList)
        Log.d("PlanTripActivity", "✅ Updated places with favorite status!")
    }

    override fun onResume() {
        super.onResume()
        updateFavoritesStatus()
    }

    private fun addPlaceToRoute(place: Place) {
        if (!selectedRoutePlaces.contains(place)) {
            selectedRoutePlaces.add(place)
            Log.d("ROUTE", "📍 Added to route: ${place.name}")
        } else {
            Log.d("ROUTE", "⚠️ Place already in route")
        }
    }



}


