// ChatActivity.java
package com.team5.taketac;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // 채팅방별 메시지 저장 (임시 메모리)
    private static final Map<String, List<Message>> chatRoomMessages = new HashMap<>();
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        String chatRoomName = getIntent().getStringExtra("chatRoomName");
        setTitle(chatRoomName); // 상단 제목에 방 이름 표시

        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        messageList = chatRoomMessages.containsKey(chatRoomId)
                ? chatRoomMessages.get(chatRoomId)
                : new ArrayList<>();

        adapter = new com.team5.taketac.adapter.MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputMessage.getText().toString().trim();
                if (!text.isEmpty()) {
                    Message message = new Message(text, true);
                    messageList.add(message);
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                    inputMessage.setText("");
                    chatRoomMessages.put(chatRoomId, messageList);
                }
            }
        });
    }
}
