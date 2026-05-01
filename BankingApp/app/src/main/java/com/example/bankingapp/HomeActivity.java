package com.example.bankingapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String username = getIntent().getStringExtra("username");

        TextView welcomeText = findViewById(R.id.welcomeText);
        if (username != null) {
            welcomeText.setText("Hello " + username + "!");
        } else {
            welcomeText.setText("Hello user!");
        }
    }
}