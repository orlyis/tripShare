package com.example.tripshare.viewmodels


import com.google.android.gms.maps.model.LatLng

data class Place(
    val placeId: String,
    val name: String,
    val categories: List<String>,
    val imageUrl: String?,
    val position: LatLng,
    var isFavorite: Boolean = false // נוסיף פרמטר כדי לבדוק אם המקום מועדף
)