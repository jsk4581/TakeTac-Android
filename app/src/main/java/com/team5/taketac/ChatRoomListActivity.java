// ChatRoomListActivity.java
package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team5.taketac.adapter.ChatRoomAdapter;
import com.team5.taketac.model.ChatRoom;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText inputRoomName;
    private Button createRoomButton;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> chatRoomList;
    private DatabaseReference chatRoomsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar); // 툴바를 액션바로 지정
        getSupportActionBar().setTitle("채팅목록"); // 화면 이름 지정
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼


        recyclerView = findViewById(R.id.recyclerViewRooms);
        inputRoomName = findViewById(R.id.inputRoomName);
        createRoomButton = findViewById(R.id.btnCreateRoom);


        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList, new ChatRoomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(ChatRoom room) {
                Intent intent = new Intent(ChatRoomListActivity.this, ChatActivity.class);
                intent.putExtra("chatRoomId", room.getId());
                intent.putExtra("chatRoomName", room.getName());
                startActivity(intent);
            }

            @Override
            public void onJoinClick(ChatRoom room) {
                // 참여 버튼 눌렀을 때 처리
                Intent intent = new Intent(ChatRoomListActivity.this, ChatActivity.class);
                intent.putExtra("chatRoomId", room.getId());
                intent.putExtra("chatRoomName", room.getName());
                startActivity(intent);
            }
            @Override
            public void onDeleteClick(ChatRoom room) {
                // 삭제 버튼 클릭 처리
                // 예: Firebase에서 해당 채팅방 삭제
                chatRoomsRef.child(room.getId()).removeValue();
                // 로컬 리스트에서 삭제 후 어댑터 갱신
                int index = chatRoomList.indexOf(room);
                if (index != -1) {
                    chatRoomList.remove(index);
                    adapter.notifyItemRemoved(index);
                }
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        // Firebase에서 채팅방 목록 불러오기
        chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ChatRoomList", "onDataChange: " + snapshot.getChildrenCount()); // 추가
                chatRoomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatRoom room = dataSnapshot.getValue(ChatRoom.class);
                    if (room != null) {
                        chatRoomList.add(room);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 오류 처리
            }
        });

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputRoomName.getText().toString().trim();
                if (!name.isEmpty()) {
                    String creatorUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    ChatRoom room = new ChatRoom(name, creatorUid);

                    // 🔥 Firebase에 저장
                    chatRoomsRef.child(room.getId()).setValue(room);

                    // 로컬 리스트에도 추가 (화면에 즉시 표시)
                    chatRoomList.add(room);
                    adapter.notifyItemInserted(chatRoomList.size() - 1);
                    inputRoomName.setText("");
                }
            }
        });
    }
}