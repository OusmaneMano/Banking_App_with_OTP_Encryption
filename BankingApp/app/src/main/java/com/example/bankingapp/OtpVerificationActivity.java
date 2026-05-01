package com.example.bankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OtpVerificationActivity extends AppCompatActivity {

    EditText etOtp;
    Button btnVerifyOtp;
    String username; // coming from LoginActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        username = getIntent().getStringExtra("username");

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (!otp.isEmpty()) {
                verifyOtpWithBackend(username, otp);
            } else {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtpWithBackend(String username, String otp) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.43.186:8089/verifyOtp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "username=" + URLEncoder.encode(username, "UTF-8") +
                        "&otp=" + URLEncoder.encode(otp, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                runOnUiThread(() -> {
                    if (responseCode == 200 && response.toString().contains("OTP verification successful")) {
                        Toast.makeText(this, "OTP Verified", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class).putExtra("username", username));
                        finish();
                    } else {
                        Toast.makeText(this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}