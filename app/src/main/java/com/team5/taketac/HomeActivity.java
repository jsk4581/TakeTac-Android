package com.team5.taketac;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.FrameLayout; // Fragment를 담을 컨테이너

import com.kakao.vectormap.LatLng; // 카카오맵 SDK의 LatLng 클래스 임포트

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Spinner spinnerOrigin;
    private Spinner spinnerDestination;
    private Button btnRequestMatch;
    private FrameLayout mapFragmentContainer; // MapFragment를 담을 FrameLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // activity_home.xml 레이아웃 설정

        // 뷰 요소 초기화
        spinnerOrigin = findViewById(R.id.spinner_origin);
        spinnerDestination = findViewById(R.id.spinner_destination);
        btnRequestMatch = findViewById(R.id.btn_request_match);
        mapFragmentContainer = findViewById(R.id.map_fragment_container); // MapFragment 컨테이너 초기화

        // 스피너에 데이터 설정
        setupSpinners();

        // 매칭 요청 버튼 클릭 리스너 설정
        btnRequestMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 스피너에서 선택된 출발지 이름 가져오기
                String selectedOriginName = (String) spinnerOrigin.getSelectedItem();

                // Constants.java에서 해당 이름에 맞는 LatLng 좌표 가져오기
                LatLng originCoords = Constants.STATIONS.get(selectedOriginName);

                if (originCoords != null) { // 출발지 좌표가 유효한지 확인
                    // MapFragment 인스턴스 생성
                    MapFragment mapFragment = new MapFragment();

                    // MapFragment로 전달할 Bundle 생성
                    Bundle bundle = new Bundle();
                    bundle.putDouble("origin_latitude", originCoords.latitude);
                    bundle.putDouble("origin_longitude", originCoords.longitude);
                    bundle.putString("origin_name", selectedOriginName); // 출발지 이름도 전달

                    mapFragment.setArguments(bundle); // Bundle을 MapFragment에 설정

                    // FragmentManager를 사용하여 MapFragment를 동적으로 추가 (또는 교체)
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    // 기존에 Fragment가 있다면 제거하고 새로운 Fragment를 추가/교체
                    fragmentTransaction.replace(R.id.map_fragment_container, mapFragment);
                    fragmentTransaction.addToBackStack(null); // 뒤로가기 버튼으로 Fragment 제거 가능하게
                    fragmentTransaction.commit();

                    // MapFragment 컨테이너를 보이게 설정
                    mapFragmentContainer.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(HomeActivity.this, "출발지 좌표를 찾을 수 없습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 스피너 설정 메서드
    private void setupSpinners() {
        // 출발지 (역) 스피너에 들어갈 이름 리스트 생성
        List<String> stationNames = new ArrayList<>(Constants.STATIONS.keySet());
        ArrayAdapter<String> originAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, stationNames);
        originAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(originAdapter);

        // 도착지 (건물) 스피너에 들어갈 이름 리스트 생성
        List<String> buildingNames = new ArrayList<>(Constants.BUILDINGS.keySet());
        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, buildingNames);
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestination.setAdapter(destinationAdapter);
    }

    // 뒤로가기 버튼 처리: MapFragment가 표시 중이면 제거하고, 없으면 Activity 종료
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(); // MapFragment를 스택에서 제거
            mapFragmentContainer.setVisibility(View.GONE); // MapFragment 컨테이너 숨김
        } else {
            super.onBackPressed(); // 일반적인 뒤로가기 동작 (액티비티 종료)
        }
    }
}