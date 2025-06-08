package com.team5.taketac;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.*;
import com.kakao.vectormap.*;
import com.kakao.vectormap.camera.CameraUpdate;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.*;
import com.kakao.vectormap.route.*;
import com.team5.taketac.api.KakaoDirectionsService;
import com.team5.taketac.model.KakaoDirectionsResponse;

import java.util.*;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapFragment extends Fragment {

    private MapView mapView;
    private KakaoMap kakaoMap;
    private LatLng destination;
    private String destinationName;

    private LabelManager labelManager;
    private RouteLineManager routeLineManager;
    private RouteLine currentRouteLine;

    private FusedLocationProviderClient fusedLocationClient;
    private final Handler locationHandler = new Handler();
    private final int REFRESH_INTERVAL = 10_000;
    private final Map<String, Label> userMarkers = new HashMap<>();

    private List<String> matchedUserIds;
    private boolean cameraMoved = false;

    private KakaoDirectionsService directionsService;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 홈화면 이동 버튼 동작 연결
        Button btnHome = view.findViewById(R.id.btn_homefragment);
        btnHome.setOnClickListener(v -> {
            Fragment homeFragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("is_matched", true);
            bundle.putString("selected_origin", destinationName);
            bundle.putStringArrayList("party_members", new ArrayList<>(matchedUserIds));
            homeFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });

        // Kakao Directions API 준비
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.KAKAO_DIRECTIONS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        directionsService = retrofit.create(KakaoDirectionsService.class);

        Bundle args = getArguments();
        if (args != null) {
            double lat = args.getDouble("origin_latitude", 37.4501);
            double lon = args.getDouble("origin_longitude", 127.1277);
            destination = LatLng.from(lat, lon);
            destinationName = args.getString("origin_name", "목적지");
            matchedUserIds = args.getStringArrayList("matched_user_ids");
        }

        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                Log.d("MapFragment", "onMapDestroy: 지도 뷰 소멸");
            }

            @Override
            public void onMapError(@NonNull Exception e) {
                Log.e("MapFragment", "지도 에러: " + e.getMessage());
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                labelManager = kakaoMap.getLabelManager();
                routeLineManager = kakaoMap.getRouteLineManager();
                requestLocationPermissionAndStart();
            }
        });

        return view;
    }

    private void requestLocationPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            startTrackingLoop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTrackingLoop();
        } else {
            Toast.makeText(getContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTrackingLoop() {
        locationHandler.post(new Runnable() {
            @Override
            public void run() {
                updateMyLocation();
                updateMatchedUserLocations();
                locationHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        });
    }

    private void updateMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng pos = LatLng.from(location.getLatitude(), location.getLongitude());
                moveCameraOnce(pos);
                findRoute(pos, destination); // 경로 요청
            }
        });
    }

    private void moveCameraOnce(LatLng center) {
        if (!cameraMoved && kakaoMap != null) {
            cameraMoved = true;
            CameraUpdate update = CameraUpdateFactory.newCenterPosition(center, 15);
            kakaoMap.moveCamera(update);
        }
    }

    private void updateMatchedUserLocations() {
        if (matchedUserIds == null || matchedUserIds.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String uid : matchedUserIds) {
            db.collection("users").document(uid).collection("location").document("current")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Double lat = snapshot.getDouble("latitude");
                            Double lon = snapshot.getDouble("longitude");
                            if (lat != null && lon != null) {
                                LatLng pos = LatLng.from(lat, lon);
                                addOrUpdateMarker(uid, pos);
                            }
                        }
                    });
        }
    }

    private void addOrUpdateMarker(String uid, LatLng position) {
        try {
            LabelLayer layer = labelManager.getLayer();

            // 이미 마커가 있는 경우 위치만 이동
            if (userMarkers.containsKey(uid)) {
                userMarkers.get(uid).moveTo(position);
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String nickname = uid; // 기본값: uid
                        if (doc.exists()) {
                            String nick = doc.getString("nickname");
                            if (nick != null && !nick.isEmpty()) {
                                nickname = nick;
                            }
                        }

                        LabelStyle style = LabelStyle.from(R.mipmap.ic_marker)
                                .setTextStyles(LabelTextStyle.from(32, Color.parseColor("#DB5461")));

                        LabelOptions options = LabelOptions.from(position)
                                .setTag(uid)
                                .setStyles(LabelStyles.from(style))
                                .setTexts(new LabelTextBuilder().setTexts(nickname));

                        Label label = layer.addLabel(options);
                        userMarkers.put(uid, label);
                    })
                    .addOnFailureListener(e -> Log.e("MapFragment", "닉네임 가져오기 실패: " + uid, e));

        } catch (Exception e) {
            Log.e("MapFragment", "마커 갱신 오류: " + uid, e);
        }
    }





    private void findRoute(LatLng origin, LatLng dest) {
        String auth = "KakaoAK " + Constants.KAKAO_REST_API_KEY;
        String originStr = origin.getLongitude() + "," + origin.getLatitude();
        String destStr = dest.getLongitude() + "," + dest.getLatitude();

        directionsService.getDirections(auth, originStr, destStr).enqueue(new Callback<KakaoDirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<KakaoDirectionsResponse> call,
                                   @NonNull Response<KakaoDirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<KakaoDirectionsResponse.Route> routes = response.body().getRoutes();
                    if (!routes.isEmpty()) {
                        List<LatLng> points = new ArrayList<>();
                        for (KakaoDirectionsResponse.Section section : routes.get(0).getSections()) {
                            for (KakaoDirectionsResponse.Road road : section.getRoads()) {
                                List<Double> vertexes = road.getVertexes();
                                for (int i = 0; i < vertexes.size(); i += 2) {
                                    double lon = vertexes.get(i);
                                    double lat = vertexes.get(i + 1);
                                    points.add(LatLng.from(lat, lon));
                                }
                            }
                        }

                        if (!points.isEmpty()) {
                            RouteLineStyle style = RouteLineStyle.from(10, 0xFF007AFF, 3, 0xFF000000);
                            RouteLineOptions options = RouteLineOptions.from(RouteLineSegment.from(points, RouteLineStyles.from(style)));
                            RouteLineLayer layer = routeLineManager.getLayer();
                            if (currentRouteLine != null) {
                                layer.remove(currentRouteLine);
                            }
                            currentRouteLine = layer.addRouteLine(options);
                        }
                    }
                } else {
                    Log.e("MapFragment", "경로 API 응답 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<KakaoDirectionsResponse> call, @NonNull Throwable t) {
                Log.e("MapFragment", "경로 요청 실패: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationHandler.removeCallbacksAndMessages(null);
    }
}
