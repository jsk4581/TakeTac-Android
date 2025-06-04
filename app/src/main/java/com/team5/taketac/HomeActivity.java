package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnGoToMatching = findViewById(R.id.btnGoToMatching);
        btnGoToMatching.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PublicMatchingActivity.class);
            startActivity(intent);
        });
    }
}
