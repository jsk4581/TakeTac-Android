package com.team5.taketac;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreatePublicPartyActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etLocation;
    private TextView tvDate, tvTime;
    private Button btnCreate;

    private int selYear, selMonth, selDay, selHour, selMinute;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_public_party);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        // 툴바 설정
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 뷰 바인딩
        etTitle    = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        tvDate     = findViewById(R.id.tvDate);
        tvTime     = findViewById(R.id.tvTime);
        btnCreate  = findViewById(R.id.btnCreate);

        // 현재 날짜/시간 기본값
        Calendar now = Calendar.getInstance();
        selYear   = now.get(Calendar.YEAR);
        selMonth  = now.get(Calendar.MONTH);
        selDay    = now.get(Calendar.DAY_OF_MONTH);
        selHour   = now.get(Calendar.HOUR_OF_DAY);
        selMinute = now.get(Calendar.MINUTE);

        updateDateText();
        updateTimeText();

        // 날짜 선택
        tvDate.setOnClickListener(v -> new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selYear = year;
                    selMonth = month;
                    selDay = dayOfMonth;
                    updateDateText();
                },
                selYear, selMonth, selDay
        ).show());

        // 시간 선택
        tvTime.setOnClickListener(v -> new TimePickerDialog(
                this,
                (tp, hourOfDay, minute) -> {
                    selHour = hourOfDay;
                    selMinute = minute;
                    updateTimeText();
                },
                selHour, selMinute,
                true
        ).show());

        // 생성 버튼
        btnCreate.setOnClickListener(v -> {
            Log.d("CreateParty", "생성 버튼 클릭됨");

            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

            Log.d("CreateParty", "입력값 - title: " + title + ", location: " + location);

            if (title.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "제목과 장소를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 날짜/시간 → timestamp
            Calendar cal = Calendar.getInstance();
            cal.set(selYear, selMonth, selDay, selHour, selMinute, 0);
            long timestamp = cal.getTimeInMillis();

            String dateStr = String.format("%04d-%02d-%02d", selYear, selMonth + 1, selDay);
            String timeStr = String.format("%02d:%02d", selHour, selMinute);

            String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
            if (uid == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                Log.e("CreateParty", "FirebaseAuth.getCurrentUser() == null");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("location", location);
            data.put("date", dateStr);            // 사용자 표시용
            data.put("time", timeStr);            // 사용자 표시용
            data.put("timestamp", timestamp);     // 정렬 및 표시용
            data.put("creatorId", uid);           // 삭제 권한용

            db.collection("publicParties")
                    .add(data)
                    .addOnSuccessListener(ref -> {
                        // 문서 ID도 저장
                        db.collection("publicParties").document(ref.getId()).update("id", ref.getId());
                        Toast.makeText(this, "공개 파티 생성 완료", Toast.LENGTH_SHORT).show();
                        Log.d("CreateParty", "파티 생성 성공 - ID: " + ref.getId());
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CreateParty", "파티 생성 실패", e);
                        Toast.makeText(this, "생성 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void updateDateText() {
        tvDate.setText(String.format("%04d-%02d-%02d", selYear, selMonth + 1, selDay));
    }

    private void updateTimeText() {
        tvTime.setText(String.format("%02d:%02d", selHour, selMinute));
    }
}
