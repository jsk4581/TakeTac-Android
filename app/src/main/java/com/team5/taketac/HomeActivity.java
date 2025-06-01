package com.team5.taketac;

import android.content.Intent; // Intent를 사용하기 위해 추가
import android.os.Bundle;
import android.view.View;     // View를 사용하기 위해 추가
import android.widget.Button;  // Button을 사용하기 위해 추가
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // activity_home.xml 레이아웃 파일을 사용

        // XML 레이아웃에 정의된 버튼 ID (buttonGoToTimetable)를 사용하여 버튼 객체를 찾습니다.
        Button buttonGoToTimetable = findViewById(R.id.buttonGoToTimetable);

        // 버튼 클릭 리스너를 설정합니다.
        buttonGoToTimetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TimetableActivity로 이동하기 위한 Intent 생성
                // 여기서 TimetableActivity.class는 실제 시간표를 보여주는 Activity의 클래스명이어야 합니다.
                // 만약 다른 이름으로 Activity를 만드셨다면 해당 이름으로 변경해주세요.
                Intent intent = new Intent(HomeActivity.this, TimetableActivity.class);
                startActivity(intent); // Intent 실행하여 화면 전환
            }
        });

        // 만약 XML에서 android:onClick="goToTimetableScreen" 속성을 사용했다면
        // 아래와 같은 메소드를 HomeActivity 내에 정의해야 합니다.
        // public void goToTimetableScreen(View view) {
        //     Intent intent = new Intent(this, TimetableActivity.class);
        //     startActivity(intent);
        // }
    }
}
