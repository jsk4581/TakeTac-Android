package com.team5.taketac;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageProfile;
    private TextView textEmail;
    private EditText editNickname, editPassword;
    private Button btnChangePassword, btnSave;

    private Uri imageUri;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // 해당 XML 먼저 만들어야 함

        imageProfile = findViewById(R.id.imageProfile);
        textEmail = findViewById(R.id.textEmail);
        editNickname = findViewById(R.id.editNickname);
        editPassword = findViewById(R.id.editPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSave = findViewById(R.id.btnSave);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Load user data
        loadUserInfo();

        imageProfile.setOnClickListener(v -> openImagePicker());

        btnChangePassword.setOnClickListener(v -> {
            String newPassword = editPassword.getText().toString();
            if (newPassword.length() < 6) {
                Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "비밀번호 변경 실패", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void loadUserInfo() {
        textEmail.setText(currentUser.getEmail());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nickname = snapshot.child("nickname").getValue(String.class);
                    String profileUrl = snapshot.child("profileUrl").getValue(String.class);

                    editNickname.setText(nickname);
                    if (profileUrl != null) {
                        Glide.with(ProfileActivity.this).load(profileUrl).into(imageProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "유저 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        progressDialog.setMessage("저장 중...");
        progressDialog.show();

        String nickname = editNickname.getText().toString().trim();

        if (imageUri != null) {
            // 프로필 이미지 업로드
            StorageReference fileRef = storageRef.child(currentUser.getUid() + ".jpg");
            fileRef.putFile(imageUri).continueWithTask(task -> {
                if (!task.isSuccessful()) throw task.getException();
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String imageUrl = task.getResult().toString();
                    updateUserProfile(nickname, imageUrl);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateUserProfile(nickname, null);
        }
    }

    private void updateUserProfile(String nickname, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nickname", nickname);
        if (imageUrl != null) {
            updates.put("profileUrl", imageUrl);
        }

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
                finish(); // <-- 저장 후 현재 액티비티 종료 (이전 화면으로 이동)
            } else {
                Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
