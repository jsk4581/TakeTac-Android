package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private TextView errorText;
    private Button loginBtn, signupBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.loginErrorText);
        loginBtn = findViewById(R.id.loginButton);
        signupBtn = findViewById(R.id.signupButton);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                errorText.setText("이메일과 비밀번호를 모두 입력해주세요.");
                return;
            }

            if (!email.endsWith("@gachon.ac.kr")) {
                errorText.setText("가천대학교 이메일(@gachon.ac.kr)만 사용할 수 있습니다.");
                return;
            }

            if (password.length() < 6) {
                errorText.setText("비밀번호는 최소 6자 이상이어야 합니다.");
                return;
            }

            errorText.setText("로그인 시도 중...");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null && user.isEmailVerified()) {
                                // 인증 완료된 경우에만 Firestore에 최초 회원 정보 저장
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(user.getUid()).get()
                                        .addOnSuccessListener(document -> {
                                            if (!document.exists()) {
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("email", user.getEmail());
                                                userData.put("createdAt", System.currentTimeMillis());
                                                db.collection("users").document(user.getUid()).set(userData);
                                            }
                                        });

                                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this,MainActivity.class));
                                finish();

                            } else {
                                errorText.setText("이메일 인증 후 로그인할 수 있습니다.");
                                mAuth.signOut();
                            }
                        } else {
                            errorText.setText("로그인 실패: " + task.getException().getMessage());
                        }
                    });
        });

        signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
