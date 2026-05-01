package com.example.otpreceiverapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SpecialCodeActivity : AppCompatActivity() {

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_special_code)

        username = intent.getStringExtra("username") ?: ""

        val codeField = findViewById<EditText>(R.id.codeField)
        val verifyButton = findViewById<Button>(R.id.verifyButton)

        verifyButton.setOnClickListener {
            val code = codeField.text.toString().trim()

            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter the special code", Toast.LENGTH_SHORT).show()
            } else {
                verifySpecialCode(code)
            }
        }
    }

    private fun verifySpecialCode(code: String) {
        val url = "http://192.168.43.186:8089/verifyAppASpecialCode"
        val requestBody = "specialCode=${URLEncoder.encode(code, "UTF-8")}"

        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                conn.outputStream.use {
                    it.write(requestBody.toByteArray())
                }

                val responseCode = conn.responseCode
                val response = conn.inputStream.bufferedReader().use { it.readText() }

                runOnUiThread {
                    if (responseCode == 200 && response.startsWith("VALID:")) {
                        // Extract username from response
                        val usernameFromServer = response.substringAfter("VALID:").trim()

                        saveUsernameLocally(usernameFromServer)
                        goToOtpScreen()
                    } else {
                        Toast.makeText(this, "Invalid special code: $response", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun saveUsernameLocally(username: String) {
        val sharedPref = getSharedPreferences("AppAPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("username", username).apply()
    }

    private fun goToOtpScreen() {
        val intent = Intent(this, GetOtpActivity::class.java)
        startActivity(intent)
        finish()
    }
}