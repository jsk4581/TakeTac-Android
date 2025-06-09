package com.team5.taketac;

import static android.content.Intent.getIntent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.team5.taketac.adapter.MessageAdapter;
import com.team5.taketac.model.Message;
import com.team5.taketac.model.PartyRoom;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private Button sendButton;
    private ImageButton recommendButton;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private TextView receiverNameView;

    private static final Map<String, List<Message>> chatRoomMessages = new HashMap<>();
    private String chatRoomId;

    private String myNickname = "나";
    private DatabaseReference messagesRef;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firestore = FirebaseFirestore.getInstance();

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        String chatRoomName = getIntent().getStringExtra("chatRoomName");
        setTitle(chatRoomName);


        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        recommendButton = findViewById(R.id.recommendButton);
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
                    .getReference("partyRooms").child(chatRoomId).child("users");

            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> nicknames = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String uid = child.getKey();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String nickname = userSnapshot.child("nickname").getValue(String.class);
                                if (nickname != null) {
                                    if (uid.equals(myUid)) {
                                        nicknames.add("나");
                                    } else {
                                        nicknames.add(nickname);
                                    }
                                }

                                if (nicknames.size() == snapshot.getChildrenCount()) {
                                    String joined = String.join(", ", nicknames);
                                    receiverNameView.setText("참여 인원: " + joined);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("ChatActivity", "닉네임 불러오기 실패", error.toException());
                            }
                        });
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

        sendButton.setOnClickListener(v -> {
            String text = inputMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Message message = new Message(text, senderUid, myNickname);
                messagesRef.push().setValue(message)
                        .addOnSuccessListener(aVoid -> inputMessage.setText(""))
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this, "메시지 전송 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("ChatActivity", "메시지 저장 실패", e);
                        });
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
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        recommendButton.setOnClickListener(v -> {
            String chatHistory = getChatMessagesAsText();
            fetchUserTimetableJsonAndRequestAI(chatHistory);
        });
    }

    private String getChatMessagesAsText() {
        StringBuilder sb = new StringBuilder();
        for (Message msg : messageList) {
            sb.append(msg.getSenderNickname()).append(": ").append(msg.getText()).append("\n");
        }
        return sb.toString();
    }

    private void fetchUserTimetableJsonAndRequestAI(String chatHistory) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "사용자 인증이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        CollectionReference scheduleRef = firestore.collection("users").document(userId).collection("schedule");

        scheduleRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, JSONArray> timetableMap = new HashMap<>();

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    ScheduleEntry entry = doc.toObject(ScheduleEntry.class);
                    try {
                        String day = entry.getDay().toLowerCase();
                        String start = formatTime(entry.getStartTime());
                        String end = formatTime(entry.getEndTime());

                        JSONArray periods = timetableMap.getOrDefault(day, new JSONArray());
                        JSONObject period = new JSONObject();
                        period.put("start", start);
                        period.put("end", end);
                        period.put("subject", entry.getSubjectName());
                        periods.put(period);

                        timetableMap.put(day, periods);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    JSONObject timetableJson = new JSONObject();
                    for (String day : timetableMap.keySet()) {
                        timetableJson.put(day, timetableMap.get(day));
                    }

                    requestAIRecommendation(chatHistory, timetableJson.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "시간표 변환 오류", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "시간표 불러오기 실패", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void requestAIRecommendation(String chatHistory, String timetableJson) {
        AIHelper.getRecommendedTime(chatHistory, timetableJson, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "AI 요청 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String result = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    try {
                        JSONObject resultJson = new JSONObject(result);
                        String departureTime = resultJson.optString("출발시간");
                        String departurePlace = resultJson.optString("출발장소");
                        String arrivalPlace = resultJson.optString("도착장소");
                        String reason = resultJson.optString("판단근거");

                        String formattedResult = "출발시간 : " + departureTime +
                                "\n출발장소 : " + departurePlace +
                                "\n도착장소 : " + arrivalPlace +
                                "\n판단근거 : " + reason;

                        runOnUiThread(() -> showRecommendationDialog(formattedResult));
                    } catch (Exception e) {
                        String cleanedResult = result.replace("{", "").replace("}", "");
                        runOnUiThread(() -> showRecommendationDialog(cleanedResult));
                    }
                } catch (Exception e) {
                    Log.d("AI_RESPONSE", responseBody);
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "AI 응답 처리 실패", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String formatTime(int timeInt) {
        return String.format("%02d:%02d", timeInt / 100, timeInt % 100);
    }

    private void showRecommendationDialog(String recommendation) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("추천 시간")
                .setMessage(recommendation)
                .setPositiveButton("확인", null)
                .show();
    }
}
