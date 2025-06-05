package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnChatRooms = findViewById(R.id.btnChatRooms);
        btnChatRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ChatRoomListActivity.class);
                startActivity(intent);
            }
        });
        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_profile = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent_profile);
            }
        });
        Button btnGoToMatching = findViewById(R.id.btnGoToMatching);
        btnGoToMatching.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PublicMatchingActivity.class);
            startActivity(intent);
        });

    }
}
