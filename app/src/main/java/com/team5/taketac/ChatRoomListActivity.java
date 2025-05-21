// ChatRoomListActivity.java
package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        recyclerView = findViewById(R.id.recyclerViewRooms);
        inputRoomName = findViewById(R.id.inputRoomName);
        createRoomButton = findViewById(R.id.btnCreateRoom);

        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList, room -> {
            Intent intent = new Intent(ChatRoomListActivity.this, ChatActivity.class);
            intent.putExtra("chatRoomId", room.getId());
            intent.putExtra("chatRoomName", room.getName());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputRoomName.getText().toString().trim();
                if (!name.isEmpty()) {
                    ChatRoom room = new ChatRoom(name);
                    chatRoomList.add(room);
                    adapter.notifyItemInserted(chatRoomList.size() - 1);
                    inputRoomName.setText("");
                }
            }
        });
    }
}