package com.team5.taketac;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team5.taketac.adapter.MessageAdapter;
import com.team5.taketac.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText inputMessage;
    private Button sendButton;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private TextView receiverNameView;

    private static final Map<String, List<Message>> chatRoomMessages = new HashMap<>();
    private String chatRoomId;

    private String myNickname = "";  // 내 닉네임 저장용
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        String chatRoomName = getIntent().getStringExtra("chatRoomName");
        setTitle(chatRoomName);

        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        receiverNameView = findViewById(R.id.receiverName);

        messageList = chatRoomMessages.containsKey(chatRoomId)
                ? chatRoomMessages.get(chatRoomId)
                : new ArrayList<>();

        adapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String myUid = currentUser.getUid();

            // 내 닉네임 불러오기
            DatabaseReference myUserRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(myUid);

            myUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nickname = snapshot.child("nickname").getValue(String.class);
                    if (nickname != null) {
                        myNickname = nickname;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

            // 상대방 닉네임 불러오기
            DatabaseReference roomRef = FirebaseDatabase.getInstance()
                    .getReference("chatRooms").child(chatRoomId).child("users");

            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String uid = child.getKey();
                        if (!uid.equals(myUid)) {
                            loadUserNickname(uid);
                            break;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        messagesRef = FirebaseDatabase.getInstance()
                .getReference("chatRooms")
                .child(chatRoomId)
                .child("messages");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputMessage.getText().toString().trim();
                if (!text.isEmpty()) {
                    Message message = new Message(text, true, myNickname);

                    messagesRef.push().setValue(message)
                            .addOnSuccessListener(aVoid -> {
                                inputMessage.setText("");  // 전송 성공 시 입력창 초기화
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ChatActivity.this, "메시지 전송 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("ChatActivity", "메시지 저장 실패", e);
                            });
                }
            }
        });


        // Firebase에서 메시지 실시간 불러오기
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 에러 처리 (필요시)
            }
        });

    }

    private void loadUserNickname(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nickname = snapshot.child("nickname").getValue(String.class);
                if (nickname != null) {
                    receiverNameView.setText("상대방: " + nickname);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
