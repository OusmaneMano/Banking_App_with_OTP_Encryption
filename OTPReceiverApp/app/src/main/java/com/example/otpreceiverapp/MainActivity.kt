package com.example.otpreceiverapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("AppAPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        if (username.isNullOrEmpty()) {
            // User not logged in → Go to Login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // User already logged in → Go to OTP screen
            val intent = Intent(this, GetOtpActivity::class.java)
            startActivity(intent)
        }

        finish() // Close MainActivity so user can't go back here
    }
}