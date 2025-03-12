package com.example.tripshare.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tripshare.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpLink = findViewById<TextView>(R.id.signUpLink)

        signUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        CoroutineScope(Dispatchers.Main).launch {
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        SplashActivity::class.java
                                    )
                                )
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login Failed: ${task.exception?.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }
    }
}