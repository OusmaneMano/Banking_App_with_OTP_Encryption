package com.example.otpreceiverapp

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GetOtpActivity : AppCompatActivity() {

    private lateinit var otpTextView: TextView
    private lateinit var username: String
    private val handler = Handler(Looper.getMainLooper())
    private val fetchInterval: Long = 5000 // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_otp)

        otpTextView = findViewById(R.id.otpTextView)

        val sharedPref = getSharedPreferences("AppAPrefs", Context.MODE_PRIVATE)
        username = sharedPref.getString("username", "") ?: ""

        if (username.isEmpty()) {
            Toast.makeText(this, "Username not found. Please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        startFetchingOtp()
    }

    private fun startFetchingOtp() {
        handler.post(object : Runnable {
            override fun run() {
                fetchLatestOtp()
                handler.postDelayed(this, fetchInterval)
            }
        })
    }

    private fun fetchLatestOtp() {
        val url = "http://192.168.43.186:8089/getLatestOtp?username=${URLEncoder.encode(username, "UTF-8")}"

        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                val responseCode = conn.responseCode
                val response = conn.inputStream.bufferedReader().use { it.readText() }

                runOnUiThread {
                    if (responseCode == 200 && response != "NO_OTP") {
                        otpTextView.text = "OTP: $response"
                    } else {
                        otpTextView.text = "Waiting for OTP..."
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    otpTextView.text = "Waiting for OTP..."
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}