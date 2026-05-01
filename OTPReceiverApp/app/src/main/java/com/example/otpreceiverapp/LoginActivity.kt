package com.example.otpreceiverapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameField = findViewById<EditText>(R.id.usernameField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginButtonAppA = findViewById<Button>(R.id.loginButtonAppA1)

        loginButtonAppA.setOnClickListener(View.OnClickListener { v: View? ->
            val username: String = usernameField.getText().toString().trim { it <= ' ' }
            val password: String = passwordField.getText().toString().trim { it <= ' ' }
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
            } else {
                performAppALogin(username, password)
            }
        })
    }

    private fun performAppALogin(username: String, password: String) {
        val url = "http://192.168.43.186:8089/loginAppA"
        val requestBody = "username=${URLEncoder.encode(username, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"

        Thread {
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                urlConnection.outputStream.use { os ->
                    os.write(requestBody.toByteArray())
                }

                val responseCode = urlConnection.responseCode
                val response = urlConnection.inputStream.bufferedReader().use { it.readText() }

                runOnUiThread {
                    if (responseCode == 200 && response == "LoginAppA success") {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        goToSpecialCodeScreen(username)
                    } else {
                        Toast.makeText(this, "Login failed: $response", Toast.LENGTH_LONG).show()
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

    private fun goToSpecialCodeScreen(username: String) {
        val intent = Intent(this, SpecialCodeActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        finish()
    }
}