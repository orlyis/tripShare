package com.example.tripshare.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // כפתור מעבר למסך תכנון הטיול
        val btnGoToPlanTrip = findViewById<Button>(R.id.btnGoToPlanTrip)
        btnGoToPlanTrip.setOnClickListener {
            val intent = Intent(this, PlanTripActivity::class.java)
            startActivity(intent)
        }
    }
}
