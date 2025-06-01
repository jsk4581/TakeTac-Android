package com.team5.taketac; // 실제 프로젝트의 패키지명으로 수정하세요

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log; // Log 사용을 위해 추가
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.team5.taketac.ScheduleEntry;
import com.team5.taketac.DisplayableItem;
import com.team5.taketac.ItemType;


public class TimetableActivity extends AppCompatActivity {

    private static final String TAG = "TimetableActivity_DEBUG"; // Log 태그

    private RecyclerView recyclerViewTimetable;
    private TimetableAdapter timetableAdapter;
    private Button buttonAddSchedule;
    private List<ScheduleEntry> originalScheduleList; // 원본 데이터
    private List<DisplayableItem> displayableItemList; // RecyclerView에 표시될 데이터

    // 시간표 범위 및 요일 설정 (프로젝트에 맞게 조절 가능)
    private static final int MIN_HOUR = 8; // 시간표 시작 시간 (예: 8시)
    private static final int MAX_HOUR = 18; // 시간표 종료 시간 (예: 18시는 18:00 ~ 18:59 강의까지 포함)
    private static final String[] DAYS_OF_WEEK = {"MON", "TUE", "WED", "THU", "FRI"}; // 토, 일 필요시 추가
    private static final int NUM_DAYS = DAYS_OF_WEEK.length;
    private static final int SPAN_COUNT = 1 + NUM_DAYS; // 1 (시간 열) + 요일 수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Log.d(TAG, "onCreate: Activity created");

        recyclerViewTimetable = findViewById(R.id.recyclerViewTimetable);
        buttonAddSchedule = findViewById(R.id.buttonAddSchedule);

        originalScheduleList = new ArrayList<>();
        displayableItemList = new ArrayList<>();

        // 어댑터 및 레이아웃 매니저 설정
        timetableAdapter = new TimetableAdapter(this, new ArrayList<>(), SPAN_COUNT);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
        // SpanSizeLookup은 모든 아이템이 1 span을 차지하므로 여기서는 기본 설정 사용

        recyclerViewTimetable.setLayoutManager(gridLayoutManager);
        recyclerViewTimetable.setAdapter(timetableAdapter);
        // recyclerViewTimetable.setHasFixedSize(true); // 아이템 크기가 변경되지 않으면 성능 향상 (선택 사항)

        Log.d(TAG, "onCreate: RecyclerView and Adapter initialized.");

        loadSchedules(); // 저장된 데이터 불러오기 (현재는 예시 데이터)
        prepareAndDisplayTimetable(); // 데이터 변환 및 UI 업데이트

        buttonAddSchedule.setOnClickListener(v -> {
            Log.d(TAG, "buttonAddSchedule clicked");
            showAddScheduleDialog();
        });
        Log.d(TAG, "Final displayableItemList size: " + displayableItemList.size());
    }

    private void prepareAndDisplayTimetable() {
        Log.d(TAG, "prepareAndDisplayTimetable: Preparing display list. Original list size: " + originalScheduleList.size());
        displayableItemList.clear();

        // 시간표 데이터를 (시간, 요일) 기반의 맵으로 변환 (빠른 조회용)
        Map<String, ScheduleEntry> scheduleMap = new HashMap<>();
        Map<String, Boolean> continuationMap = new HashMap<>(); // 연속된 강의인지 표시

        // 원본 데이터를 정렬 (요일, 시작시간 순) - 선택 사항이지만, 맵 생성 시 덮어쓰기 방지에 도움될 수 있음
        Collections.sort(originalScheduleList, Comparator.comparing(ScheduleEntry::getDay)
                .thenComparingInt(ScheduleEntry::getStartTime));

        Log.d(TAG, "--- Building scheduleMap from originalScheduleList (size: " + originalScheduleList.size() + ") ---");
        for (ScheduleEntry entry : originalScheduleList) {
            Log.d(TAG, "Processing entry for map: " + entry.getSubjectName() + " on " + entry.getDay() + " from " + entry.getStartTime() + " to " + entry.getEndTime());
            for (int hour = entry.getStartTime(); hour < entry.getEndTime(); hour++) {
                String key = entry.getDay() + "_" + hour;
                scheduleMap.put(key, entry);
                if (hour > entry.getStartTime()) {
                    continuationMap.put(key, true); // 첫 시간 이후는 연속된 블록
                } else {
                    continuationMap.put(key, false); // 첫 시간 블록
                }
            }
        }
        Log.d(TAG, "--- scheduleMap built. Size: " + scheduleMap.size() + " ---");

        // RecyclerView에 표시될 아이템 리스트 생성
        Log.d(TAG, "--- Building displayableItemList ---");
        for (int hour = MIN_HOUR; hour <= MAX_HOUR; hour++) {
            // 1. 시간 레이블 추가
            displayableItemList.add(new DisplayableItem(ItemType.TIME_LABEL, String.format("%02d:00", hour)));
            // Log.d(TAG, "Added TIME_LABEL for " + String.format("%02d:00", hour));


            // 2. 각 요일별 셀 추가
            for (String day : DAYS_OF_WEEK) {
                String key = day + "_" + hour;
                if (scheduleMap.containsKey(key)) {
                    ScheduleEntry entry = scheduleMap.get(key);
                    boolean isContinuation = continuationMap.getOrDefault(key, false);
                    displayableItemList.add(new DisplayableItem(ItemType.SCHEDULE_ENTRY, entry, isContinuation));
                    Log.d(TAG, "Added SCHEDULE_ENTRY for " + key + ": " + entry.getSubjectName() + (isContinuation ? " (cont.)" : ""));
                } else {
                    displayableItemList.add(new DisplayableItem(ItemType.EMPTY_SLOT, "")); // 빈 칸
                    // Log.d(TAG, "Added EMPTY_SLOT for " + key);
                }
            }
        }
        Log.d(TAG, "--- displayableItemList built. Size: " + displayableItemList.size() + " ---");
        timetableAdapter.updateData(displayableItemList);
        Log.d(TAG, "Adapter data updated.");
    }

    private void showAddScheduleDialog() {
        Log.d(TAG, "showAddScheduleDialog: Dialog showing");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // dialog_add_schedule.xml 파일이 res/layout 폴더에 있어야 합니다.
        View dialogView = inflater.inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        final EditText editTextSubjectName = dialogView.findViewById(R.id.editTextSubjectName);
        final EditText editTextClassroom = dialogView.findViewById(R.id.editTextClassroom);
        final EditText editTextDay = dialogView.findViewById(R.id.editTextDay);
        final EditText editTextStartTime = dialogView.findViewById(R.id.editTextStartTime);
        final EditText editTextEndTime = dialogView.findViewById(R.id.editTextEndTime);

        builder.setTitle("시간표 추가");
        builder.setPositiveButton("추가", (dialog, which) -> {
            Log.d(TAG, "Dialog '추가' button clicked");

            String subjectName = editTextSubjectName.getText().toString().trim();
            String classroom = editTextClassroom.getText().toString().trim();
            String day = editTextDay.getText().toString().trim().toUpperCase();
            String startTimeStr = editTextStartTime.getText().toString().trim();
            String endTimeStr = editTextEndTime.getText().toString().trim();

            Log.d(TAG, "Input values: Subject=" + subjectName + ", Classroom=" + classroom + ", Day=" + day + ", Start=" + startTimeStr + ", End=" + endTimeStr);

            if (subjectName.isEmpty() || classroom.isEmpty() || day.isEmpty() || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
                Log.w(TAG, "Validation failed: Empty fields");
                Toast.makeText(TimetableActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int startTime = Integer.parseInt(startTimeStr);
                int endTime = Integer.parseInt(endTimeStr);

                // 시간 유효성 검사 (예: 시작 시간 < 종료 시간, 유효한 시간 범위, 유효한 요일)
                // endTime은 MAX_HOUR + 1 까지 허용 (예: 18시 종료는 18이므로, MAX_HOUR가 18일 때 endTime은 19까지 가능)
                if (startTime >= endTime || startTime < MIN_HOUR || endTime > MAX_HOUR + 1 || !isValidDay(day)) {
                    Log.w(TAG, "Validation failed: Invalid time or day. Start=" + startTime + ", End=" + endTime + ", Day=" + day);
                    Toast.makeText(TimetableActivity.this, "입력값을 확인해주세요 (시간, 요일).", Toast.LENGTH_SHORT).show();
                    return;
                }

                // (선택 사항) 시간 중복 검사 로직 추가 가능
                // ...

                ScheduleEntry newEntry = new ScheduleEntry(day, startTime, endTime, subjectName, classroom);
                Log.d(TAG, "New ScheduleEntry created: " + newEntry.getSubjectName() + " on " + newEntry.getDay() + " " + newEntry.getStartTime() + "-" + newEntry.getEndTime());
                addNewScheduleEntry(newEntry);
                Toast.makeText(TimetableActivity.this, "시간표가 추가되었습니다.", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Log.e(TAG, "NumberFormatException for time input: " + e.getMessage());
                Toast.makeText(TimetableActivity.this, "시간은 숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> Log.d(TAG, "Dialog '취소' button clicked"));
        builder.create().show();
    }

    private boolean isValidDay(String day) {
        for (String validDay : DAYS_OF_WEEK) {
            if (validDay.equals(day)) return true;
        }
        return false;
    }

    private void loadSchedules() {
        Log.d(TAG, "loadSchedules: Loading initial data.");
        // TODO: SharedPreferences, SQLite, Room 등에서 데이터 불러오는 로직 구현
        // 이 부분은 데이터 영구 저장 시 중요합니다. 현재는 임시 데이터 사용.
        originalScheduleList.clear(); // 중복 방지
        originalScheduleList.add(new ScheduleEntry("MON", 9, 11, "안드로이드 프로그래밍", "공학관 101호"));
        originalScheduleList.add(new ScheduleEntry("WED", 14, 16, "자료구조", "정보관 202호"));
        originalScheduleList.add(new ScheduleEntry("FRI", 10, 12, "운영체제", "미래관 303호"));
        originalScheduleList.add(new ScheduleEntry("MON", 14, 15, "웹프로그래밍", "IT관 505호"));
        Log.d(TAG, "loadSchedules: Initial data loaded. Size: " + originalScheduleList.size());
    }

    private void addNewScheduleEntry(ScheduleEntry entry) {
        Log.d(TAG, "addNewScheduleEntry: Adding entry - " + entry.getSubjectName() + ". Current list size: " + originalScheduleList.size());
        originalScheduleList.add(entry);
        Log.d(TAG, "Entry added. New list size: " + originalScheduleList.size());
        // TODO: (매우 중요) 변경된 originalScheduleList를 영구 저장소에 저장하는 로직 추가
        // 예: saveSchedulesToPreferences(); 또는 데이터베이스에 저장
        prepareAndDisplayTimetable(); // UI 다시 그리기
    }
}