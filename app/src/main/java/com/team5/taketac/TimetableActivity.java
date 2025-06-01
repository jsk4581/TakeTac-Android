package com.team5.taketac;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private static final String TAG = "TimetableActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable); // 💡 XML에 맞게 수정 필요

        recyclerView = findViewById(R.id.recyclerViewTimetable);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 6)); // 6열 (시간 + 월~금)

        // 🔥 Firestore에서 시간표 데이터 로드
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e(TAG, "사용자 인증 실패");
            return;
        }

        String uid = user.getEmail();

        db.collection("users").document(uid).collection("timetable")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ScheduleEntry> scheduleEntries = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ScheduleEntry entry = doc.toObject(ScheduleEntry.class);
                        entry.setId(doc.getId());
                        scheduleEntries.add(entry);
                        Log.d("FIRESTORE", "doc = " + doc.getData());
                    }

                    List<DisplayableItem> displayableList = generateDisplayItems(scheduleEntries);
                    Log.d("CHECK", "displayableList size: " + displayableList.size());

                    TimetableAdapter adapter = new TimetableAdapter(this, displayableList, 6);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore 불러오기 실패: " + e.getMessage());
                });
    }

    // 🔥 시간표를 6열 * 13행으로 구성
    private List<DisplayableItem> generateDisplayItems(List<ScheduleEntry> entries) {
        List<DisplayableItem> result = new ArrayList<>();

        String[] days = {"TIME", "MON", "TUE", "WED", "THU", "FRI"};
        int startHour = 8, endHour = 21;

        Map<String, DisplayableItem> grid = new HashMap<>();

        for (ScheduleEntry entry : entries) {
            for (int hour = entry.getStartTime(); hour < entry.getEndTime(); hour++) {
                String key = entry.getDay() + "_" + hour;
                boolean isContinuation = hour != entry.getStartTime();
                grid.put(key, new DisplayableItem(ItemType.SCHEDULE_ENTRY, entry, isContinuation));
                Log.d("CHECK", "put key=" + key + ", cont=" + isContinuation);
            }
        }

        for (int hour = startHour; hour < endHour; hour++) {
            for (String day : days) {
                if (day.equals("TIME")) {
                    result.add(new DisplayableItem(ItemType.TIME_LABEL, hour + ":00"));
                } else {
                    String key = day + "_" + hour;
                    DisplayableItem item = grid.getOrDefault(key,
                            new DisplayableItem(ItemType.EMPTY_SLOT, (ScheduleEntry) null, false));

                    if (item.type == ItemType.SCHEDULE_ENTRY) {
                        Log.d("CHECK", "display entry: " + key);
                    }

                    result.add(item);
                }
            }
        }

        return result;
    }
}










