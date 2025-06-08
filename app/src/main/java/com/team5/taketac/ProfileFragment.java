package com.team5.taketac;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private EditText editTextNickname, editTextPassword;
    private TextView viewEmail; // 이메일 표시용 TextView
    private Button buttonSave;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    public ProfileFragment() {
        // 기본 생성자 필수
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        viewEmail = view.findViewById(R.id.viewemail);
        editTextNickname = view.findViewById(R.id.editTextNickname);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        buttonSave = view.findViewById(R.id.buttonSave);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid());

            // 사용자 데이터 불러오기
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nickname = snapshot.child("nickname").getValue(String.class);
                    if (nickname != null) {
                        editTextNickname.setText(nickname);
                    }

                    // 비밀번호는 보통 이렇게 불러오지 않고, FirebaseAuth에서 관리합니다.
                    // 필요하면 다른 UI를 구현하세요.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "사용자 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        buttonSave.setOnClickListener(v -> saveUserProfile());

        return view;
    }

    private void saveUserProfile() {
        String newNickname = editTextNickname.getText().toString().trim();
        String newPassword = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newNickname)) {
            Toast.makeText(getContext(), "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 닉네임 저장
        userRef.child("nickname").setValue(newNickname)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "닉네임이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "닉네임 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // 비밀번호 변경 (FirebaseAuth를 통해)
        if (!TextUtils.isEmpty(newPassword)) {
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        editTextPassword.setText(""); // 비밀번호 입력 필드 초기화
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "비밀번호 변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
