package com.example.tripshare.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // אתחול ה-MapFragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val defaultLocation = LatLng(32.0853, 34.7818)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        mMap.setOnMapClickListener { latLng ->
            CoroutineScope(Dispatchers.Main).launch {
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
                selectedLocation = latLng
            }
        }
    }


    // פונקציה להחזיר את המיקום שנבחר ל-PlanTripActivity
    override fun onBackPressed() {
        selectedLocation?.let {
            val resultIntent = Intent()
            resultIntent.putExtra("selected_latitude", it.latitude)
            resultIntent.putExtra("selected_longitude", it.longitude)
            setResult(Activity.RESULT_OK, resultIntent)
        }
        super.onBackPressed()
    }
}
