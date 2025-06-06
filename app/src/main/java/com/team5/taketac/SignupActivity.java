package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nicknameEditText, addressEditText;
    private Button signupConfirmButton, addressSearchButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private static final int ADDRESS_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailEditText = findViewById(R.id.signupEmailInput);
        passwordEditText = findViewById(R.id.signupPasswordInput);
        nicknameEditText = findViewById(R.id.nicknameInput);
        addressEditText = findViewById(R.id.editAddressInput);
        signupConfirmButton = findViewById(R.id.signupConfirmButton);
        addressSearchButton = findViewById(R.id.btnSearchAddress);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        addressSearchButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, KakaoSearchActivity.class);
            intent.putExtra("keyword", addressEditText.getText().toString());
            startActivityForResult(intent, ADDRESS_REQUEST_CODE);
        });

        signupConfirmButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String nickname = nicknameEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || nickname.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                String emailKey = user.getEmail();  // UID 대신 이메일 사용
                                if (emailKey == null) {
                                    Toast.makeText(this, "이메일 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 닉네임 저장
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("nickname", nickname);
                                db.collection("users").document(emailKey).set(userData);

                                // 주소 저장
                                Map<String, Object> addressData = new HashMap<>();
                                addressData.put("dong", address);
                                db.collection("users").document(emailKey)
                                        .collection("address").document("main").set(addressData);

                                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDRESS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selectedAddress");
            if (selectedAddress != null) {
                // EditText 업데이트
                addressEditText.setText(selectedAddress);

                // TextView 도 함께 업데이트
                TextView selectedTextView = findViewById(R.id.textSelectedAddress);
                selectedTextView.setText("선택된 주소: " + selectedAddress);
            }
        }
    }
}






