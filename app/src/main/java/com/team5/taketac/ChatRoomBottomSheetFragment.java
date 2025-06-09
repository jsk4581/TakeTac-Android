package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatRoomBottomSheetFragment extends BottomSheetDialogFragment {

    private final String chatRoomId;
    private final String chatRoomName;
    private final Runnable onLeave;  // 콜백: 나가기 후 목록 갱신

    public ChatRoomBottomSheetFragment(String chatRoomId, String chatRoomName, Runnable onLeave) {
        this.chatRoomId = chatRoomId;
        this.chatRoomName = chatRoomName;
        this.onLeave = onLeave;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatroom_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvChatRoomName = view.findViewById(R.id.tvChatRoomName);
        Button btnEnterChat = view.findViewById(R.id.btnEnterChat);
        Button btnLeaveChat = view.findViewById(R.id.btnLeaveChat);

        tvChatRoomName.setText(chatRoomName);

        btnEnterChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatRoomId", chatRoomId);
            intent.putExtra("chatRoomName", chatRoomName);
            startActivity(intent);
        });

        btnLeaveChat.setOnClickListener(v -> {
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            FirebaseFirestore.getInstance().collection("parties")
                    .document(chatRoomId)
                    .update("members", FieldValue.arrayRemove(userEmail))
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "채팅방에서 나갔습니다", Toast.LENGTH_SHORT).show();
                        dismiss();
                        if (onLeave != null) onLeave.run();  // 🔁 목록 새로고침 콜백
                    });
        });
    }
}
