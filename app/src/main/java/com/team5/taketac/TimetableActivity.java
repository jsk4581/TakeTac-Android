package com.team5.taketac;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class TimetableActivity extends AppCompatActivity {

    private static final String TAG = "TimetableActivity";
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private String uid;
    private TimetableAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        recyclerView = findViewById(R.id.recyclerViewTimetable);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 6));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "사용자 인증 실패");
            return;
        }

        uid = user.getEmail();
        db = FirebaseFirestore.getInstance();

        loadDataFromFirestore();

        Button addButton = findViewById(R.id.buttonAddSchedule);
        addButton.setOnClickListener(v -> showAddScheduleDialog());
    }

    private void loadDataFromFirestore() {
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

                    adapter = new TimetableAdapter(this, displayableList, 6);
                    recyclerView.setAdapter(adapter);

                    adapter.setOnItemClickListener((view, entry) -> showPopup(view, entry));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore 불러오기 실패: " + e.getMessage()));
    }

    private String generateRandomColor() {
        String[] palette = {
                "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9",
                "#C5CAE9", "#BBDEFB", "#B2EBF2", "#B2DFDB",
                "#C8E6C9", "#DCEDC8", "#F0F4C3", "#FFF9C4",
                "#FFE0B2", "#FFCCBC", "#D7CCC8", "#F5F5F5"
        };
        return palette[new Random().nextInt(palette.length)];
    }

    private void showPopup(View view, ScheduleEntry entry) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("수정");
        popup.getMenu().add("삭제");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("수정")) {
                showEditScheduleDialog(entry);
            } else if (title.equals("삭제")) {
                deleteSchedule(entry);
            }
            return true;
        });

        popup.show();
    }

    private void deleteSchedule(ScheduleEntry entry) {
        db.collection("users").document(uid).collection("timetable")
                .document(entry.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show();
                    loadDataFromFirestore();
                });
    }

    private void showEditScheduleDialog(ScheduleEntry entry) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null);
        Spinner daySpinner = dialogView.findViewById(R.id.spinnerDay);
        Spinner startSpinner = dialogView.findViewById(R.id.spinnerStartTime);
        Spinner endSpinner = dialogView.findViewById(R.id.spinnerEndTime);
        EditText subject = dialogView.findViewById(R.id.editTextSubjectName);
        EditText classroom = dialogView.findViewById(R.id.editTextClassroom);

        String[] days = {"MON", "TUE", "WED", "THU", "FRI"};
        daySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days));

        List<Integer> hours = new ArrayList<>();
        for (int i = 8; i <= 20; i++) hours.add(i);
        ArrayAdapter<Integer> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hours);
        startSpinner.setAdapter(timeAdapter);
        endSpinner.setAdapter(timeAdapter);

        daySpinner.setSelection(Arrays.asList(days).indexOf(entry.getDay()));
        startSpinner.setSelection(hours.indexOf(entry.getStartTime()));
        endSpinner.setSelection(hours.indexOf(entry.getEndTime()));
        subject.setText(entry.getSubjectName());
        classroom.setText(entry.getClassroom());

        new AlertDialog.Builder(this)
                .setTitle("시간표 수정")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    entry.setDay(daySpinner.getSelectedItem().toString());
                    entry.setStartTime((int) startSpinner.getSelectedItem());
                    entry.setEndTime((int) endSpinner.getSelectedItem());
                    entry.setSubjectName(subject.getText().toString().trim());
                    entry.setClassroom(classroom.getText().toString().trim());

                    db.collection("users").document(uid).collection("timetable")
                            .document(entry.getId()).set(entry)
                            .addOnSuccessListener(aVoid -> loadDataFromFirestore());
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showAddScheduleDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null);
        Spinner daySpinner = dialogView.findViewById(R.id.spinnerDay);
        Spinner startSpinner = dialogView.findViewById(R.id.spinnerStartTime);
        Spinner endSpinner = dialogView.findViewById(R.id.spinnerEndTime);
        EditText subject = dialogView.findViewById(R.id.editTextSubjectName);
        EditText classroom = dialogView.findViewById(R.id.editTextClassroom);

        String[] days = {"MON", "TUE", "WED", "THU", "FRI"};
        daySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days));

        List<Integer> hours = new ArrayList<>();
        for (int i = 8; i <= 20; i++) hours.add(i);
        ArrayAdapter<Integer> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hours);
        startSpinner.setAdapter(timeAdapter);
        endSpinner.setAdapter(timeAdapter);

        new AlertDialog.Builder(this)
                .setTitle("시간표 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String day = daySpinner.getSelectedItem().toString();
                    int start = (int) startSpinner.getSelectedItem();
                    int end = (int) endSpinner.getSelectedItem();
                    String sub = subject.getText().toString().trim();
                    String room = classroom.getText().toString().trim();

                    if (sub.isEmpty() || room.isEmpty() || start >= end) {
                        Toast.makeText(this, "입력값 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ScheduleEntry entry = new ScheduleEntry(day, start, end, sub, room);
                    entry.setColor(generateRandomColor());
                    db.collection("users").document(uid).collection("timetable")
                            .add(entry)
                            .addOnSuccessListener(ref -> loadDataFromFirestore());
                })
                .setNegativeButton("취소", null)
                .show();
    }

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
                    result.add(item);
                }
            }
        }

        return result;
    }
}








