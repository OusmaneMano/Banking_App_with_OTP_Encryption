package com.example.bankingapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SignUpActivity extends AppCompatActivity {

    EditText etFullName, etPhone, etUsername, etPassword;
    Button btnSignup;
    Button btnBackToLogin;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etFullName = findViewById(R.id.tFullName);
        etPhone = findViewById(R.id.tPhone);
        etUsername = findViewById(R.id.tUsername);
        etPassword = findViewById(R.id.tPassword);
        btnSignup = findViewById(R.id.tbtnSignup);
        tvResult = findViewById(R.id.ttvResult);

        btnSignup.setOnClickListener(view -> {
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fullName.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
                tvResult.setText("All fields are required.");
            } else {
                new SignupTask().execute(fullName, phone, username, password);
            }
        });
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnBackToLogin.setOnClickListener(view -> {
            finish(); // closes SignUpActivity and returns to LoginActivity
        });
    }

    private class SignupTask extends AsyncTask<String, Void, String> {

        boolean success = false;

        @Override
        protected String doInBackground(String... params) {
            try {
                String postData =
                        URLEncoder.encode("full_name", "UTF-8") + "=" + URLEncoder.encode(params[0], "UTF-8") + "&" +
                                URLEncoder.encode("phone_number", "UTF-8") + "=" + URLEncoder.encode(params[1], "UTF-8") + "&" +
                                URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(params[2], "UTF-8") + "&" +
                                URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(params[3], "UTF-8");

                URL url = new URL("http://192.168.43.186:8089/signup");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                in.close();

                if (response.toString().contains("Signup successful")) {
                    success = true;
                }

                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            tvResult.setText(result);

            if (success) {
                Toast.makeText(SignUpActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();

                // Notify LoginActivity and finish
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "Sign-up failed!", Toast.LENGTH_LONG).show();
            }
        }
    }
}