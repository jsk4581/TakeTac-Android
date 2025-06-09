package com.team5.taketac;

import static com.team5.taketac.BuildConfig.KAKAO_NATIVE_APP_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kakao.sdk.common.util.Utility;
import com.kakao.vectormap.KakaoMapSdk;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // 🔹 재사용할 프래그먼트들
    private Fragment homeFragment;
    private Fragment partyFragment;
    private Fragment timetableFragment;
    private Fragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("KeyHash", keyHash);

        // ✅ 로그인 여부 확인
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 🔹 로그인된 경우 메인 레이아웃 로딩
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigationView);

        // 🔹 프래그먼트 초기화 (한 번만 생성)
        homeFragment = new HomeFragment();
        partyFragment = new PartyFragment();
        timetableFragment = new TimetableFragment();
        profileFragment = new ProfileFragment();

        // 🔹 초기화면 설정
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        }

        // 🔹 바텀 내비게이션 클릭 처리
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (id == R.id.nav_party) {
                selectedFragment = partyFragment;
            } else if (id == R.id.nav_timetable) {
                selectedFragment = timetableFragment;
            } else if (id == R.id.nav_profile) {
                selectedFragment = profileFragment;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });
    }
}
