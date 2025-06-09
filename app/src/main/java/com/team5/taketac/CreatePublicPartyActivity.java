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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team5.taketac.model.PartyRoom;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePublicPartyActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etLocation;
    private TextView tvDate, tvTime;
    private Button btnCreate;
    private int selYear, selMonth, selDay, selHour, selMinute;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_public_party);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnCreate = findViewById(R.id.btnCreate);

        Calendar now = Calendar.getInstance();
        selYear = now.get(Calendar.YEAR);
        selMonth = now.get(Calendar.MONTH);
        selDay = now.get(Calendar.DAY_OF_MONTH);
        selHour = now.get(Calendar.HOUR_OF_DAY);
        selMinute = now.get(Calendar.MINUTE);
        updateDateText();
        updateTimeText();

        tvDate.setOnClickListener(v -> new DatePickerDialog(
                this,
                (DatePicker view, int y, int m, int d) -> {
                    selYear = y; selMonth = m; selDay = d;
                    updateDateText();
                },
                selYear, selMonth, selDay
        ).show());

        tvTime.setOnClickListener(v -> new TimePickerDialog(
                this,
                (tp, h, min) -> {
                    selHour = h; selMinute = min;
                    updateTimeText();
                },
                selHour, selMinute, true
        ).show());

        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

            if (title.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "제목과 장소를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar cal = Calendar.getInstance();
            cal.set(selYear, selMonth, selDay, selHour, selMinute, 0);
            long timestamp = cal.getTimeInMillis();

            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firestore 저장
            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("location", location);
            data.put("timestamp", timestamp);
            data.put("ownerUid", uid);

            db.collection("publicParties")
                    .add(data)
                    .addOnSuccessListener(ref -> Log.d("CreateParty", "Firestore 저장 완료"))
                    .addOnFailureListener(e -> Log.e("CreateParty", "Firestore 저장 실패", e));

            // Realtime Database 저장 (partyRooms + 방장 참여 등록)
            String partyId = UUID.randomUUID().toString();
            PartyRoom room = new PartyRoom(partyId, title, location, timestamp, uid);
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("partyRooms")
                    .child(partyId);

            ref.setValue(room);
            ref.child("participants").child(uid).setValue(true); // ✅ 방장 자동 참여

            Toast.makeText(this, "공개 파티 생성 완료", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateDateText() {
        tvDate.setText(String.format("%04d-%02d-%02d", selYear, selMonth + 1, selDay));
    }

    private void updateTimeText() {
        tvTime.setText(String.format("%02d:%02d", selHour, selMinute));
    }
}
