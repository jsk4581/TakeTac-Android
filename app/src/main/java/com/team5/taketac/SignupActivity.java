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

public class SignupActivity extends AppCompatActivity {

    private EditText nicknameInput, emailInput, passwordInput;
    private TextView errorText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nicknameInput = findViewById(R.id.nicknameInput);
        emailInput = findViewById(R.id.signupEmailInput);
        passwordInput = findViewById(R.id.signupPasswordInput);
        errorText = findViewById(R.id.signupErrorText);
        Button signupConfirmButton = findViewById(R.id.signupConfirmButton);

        mAuth = FirebaseAuth.getInstance();

        signupConfirmButton.setOnClickListener(v -> {
            String nickname = nicknameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                errorText.setText("모든 항목을 입력해주세요.");
                return;
            }

            if (!email.endsWith("@gachon.ac.kr")) {
                errorText.setText("가천대학교 이메일(@gachon.ac.kr)만 가입 가능합니다.");
                return;
            }

            if (password.length() < 6) {
                errorText.setText("비밀번호는 6자 이상이어야 합니다.");
                return;
            }

            errorText.setText("회원가입 진행 중...");

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this,
                                                    "인증 메일을 보냈습니다. 인증 후 로그인 해주세요.",
                                                    Toast.LENGTH_LONG).show();
                                            mAuth.signOut(); // 로그인 상태 초기화
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            errorText.setText("인증 메일 전송 실패: " + e.getMessage());
                                        });
                            }
                        } else {
                            errorText.setText("회원가입 실패: " + task.getException().getMessage());
                        }
                    });
        });
    }
}
