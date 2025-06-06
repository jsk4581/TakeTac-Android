package com.team5.taketac;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import com.google.firebase.firestore.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_EDIT_ADDRESS = 2;

    private ImageView imageProfile;
    private TextView textEmail, textAddress;
    private EditText editNickname, editPassword;
    private Button btnChangePassword, btnSave, btnEditAddress;

    private Uri imageUri;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageProfile = view.findViewById(R.id.imageProfile);
        textEmail = view.findViewById(R.id.textEmail);
        textAddress = view.findViewById(R.id.textAddress);
        editNickname = view.findViewById(R.id.editNickname);
        editPassword = view.findViewById(R.id.editPassword);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnSave = view.findViewById(R.id.btnSave);
        btnEditAddress = view.findViewById(R.id.btnEditAddress);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        loadUserInfo();
        loadAddress();

        imageProfile.setOnClickListener(v -> openImagePicker());

        btnChangePassword.setOnClickListener(v -> {
            String newPassword = editPassword.getText().toString();
            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "비밀번호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                Toast.makeText(getContext(), task.isSuccessful() ? "비밀번호가 변경되었습니다." : "비밀번호 변경 실패", Toast.LENGTH_SHORT).show();
            });
        });

        btnEditAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), KakaoSearchActivity.class);
            startActivityForResult(intent, REQUEST_EDIT_ADDRESS);
        });

        btnSave.setOnClickListener(v -> saveUserInfo());

        return view;
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
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nickname = snapshot.child("nickname").getValue(String.class);
                    String profileUrl = snapshot.child("profileUrl").getValue(String.class);

                    editNickname.setText(nickname);
                    if (profileUrl != null) {
                        Glide.with(requireContext()).load(profileUrl).into(imageProfile);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "유저 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAddress() {
        firestore.collection("users").document(currentUser.getEmail())
                .collection("address").document("main")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String dong = doc.getString("dong");
                        textAddress.setText(dong != null ? dong : "주소 없음");
                    } else {
                        textAddress.setText("주소 없음");
                    }
                })
                .addOnFailureListener(e -> textAddress.setText("주소 불러오기 실패"));
    }

    private void saveUserInfo() {
        progressDialog.setMessage("저장 중...");
        progressDialog.show();

        String nickname = editNickname.getText().toString().trim();

        if (imageUri != null) {
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
                    Toast.makeText(getContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), task.isSuccessful() ? "저장 완료" : "저장 실패", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                imageProfile.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_EDIT_ADDRESS && resultCode == getActivity().RESULT_OK && data != null) {
            String selectedAddress = data.getStringExtra("selectedAddress");
            if (selectedAddress != null) {
                textAddress.setText(selectedAddress);
                firestore.collection("users").document(currentUser.getEmail())
                        .collection("address").document("main")
                        .set(new HashMap<String, Object>() {{
                            put("dong", selectedAddress);
                        }});
            }
        }
    }
}








