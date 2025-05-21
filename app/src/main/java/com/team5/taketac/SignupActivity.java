package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Button signupConfirmButton = findViewById(R.id.signupConfirmButton);
        signupConfirmButton.setOnClickListener(v -> {
            // 백엔드 연동 전에는 로그인 화면으로 회귀
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
