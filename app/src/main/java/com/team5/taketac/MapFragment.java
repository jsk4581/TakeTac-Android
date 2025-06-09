package com.team5.taketac;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
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
    private static final double ARRIVAL_RADIUS_METERS = 100.0;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private static final int LOCATION_INTERVAL = 10000; // 위치 업데이트 간격 (10초)
    private static final int FASTEST_LOCATION_INTERVAL = 5000; // 가장 빠른 위치 업데이트 간격 (5초)

    private final Map<String, Label> userMarkers = new HashMap<>();

    private List<String> matchedUserIds;
    private boolean cameraMoved = false;
    private boolean isMatched = false; // <-- 이 줄 추가: 매칭 상태를 저장할 변수

    private KakaoDirectionsService directionsService;

    // Notification 관련 상수 추가
    private static final String CHANNEL_ID_MAP = "take_tac_arrival_map_channel"; // HomeFragment와 다른 ID 사용
    private static final int NOTIFICATION_ID_MAP = 102; // HomeFragment와 다른 ID 사용

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

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
            // is_matched 값 읽기 <-- 이 줄 추가
            isMatched = args.getBoolean("is_matched", false);
        }

        // 알림 채널 생성 (앱 시작 시 한 번만 호출되도록 MainActivity 또는 Application 클래스에서 호출하는 것이 더 좋음)
        // 여기서는 MapFragment가 생성될 때마다 호출되지만, 중복 생성되어도 문제는 없습니다.
        createNotificationChannel();

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

    // 알림 채널 생성 메서드 (MapFragment용)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "도착 알림 (지도)";
            String description = "지도 화면에서 출발지 도착 시 알림을 받습니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH; // 높은 중요도로 설정
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_MAP, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestLocationPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            startLocationUpdates(); // 지속적인 위치 업데이트 시작
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates(); // 권한 부여 시 지속적인 위치 업데이트 시작
        } else {
            Toast.makeText(getContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 지속적인 위치 업데이트 시작 (requestLocationUpdates 사용)
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_INTERVAL); // 10초마다 업데이트
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL); // 가능한 경우 5초마다 업데이트
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 가장 높은 정확도 요구

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w("MapFragment", "LocationResult is null.");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        LatLng pos = LatLng.from(location.getLatitude(), location.getLongitude());
                        moveCameraOnce(pos); // 카메라 한 번만 이동
                        findRoute(pos, destination); // 경로 요청 및 그리기
                        updateMatchedUserLocations(); // 매칭된 유저 위치 업데이트
                        checkArrivalInMap(location); // <-- 여기서 매칭 완료 후 도착 확인하도록 변경될 것
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper.getMainLooper() */);
            Log.d("MapFragment", "MapFragment에서 지속적인 위치 업데이트 요청 시작.");
        } else {
            Log.w("MapFragment", "MapFragment: 위치 권한이 없어 위치 업데이트를 시작할 수 없습니다.");
        }
    }

    private void moveCameraOnce(LatLng center) {
        if (!cameraMoved && kakaoMap != null) {
            cameraMoved = true;
            CameraUpdate update = CameraUpdateFactory.newCenterPosition(center, 15);
            kakaoMap.moveCamera(update);
        }
    }

    private void updateMatchedUserLocations() {
        // 'matchedUserIds'가 null이거나 비어있으면 매칭된 유저가 없다는 의미.
        // 하지만 매칭 대기 중에도 지도를 볼 수 있으므로 이 조건만으로 리턴하지 않습니다.
        // 지도에 표시할 유저가 있을 때만 Firestore 쿼리 실행
        if (matchedUserIds == null || matchedUserIds.isEmpty()) {
            // 현재 내 위치만 표시하고 싶다면, 여기에 본인 위치 마커 추가 로직을 넣을 수 있습니다.
            return;
        }

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
                    })
                    .addOnFailureListener(e -> Log.e("MapFragment", "매칭된 유저 위치 가져오기 실패: " + uid, e));
        }
    }

    private void addOrUpdateMarker(String uid, LatLng position) {
        try {
            LabelLayer layer = labelManager.getLayer();

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
                    Log.e("MapFragment", "경로 API 응답 오류: " + response.code() + ", 메시지: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<KakaoDirectionsResponse> call, @NonNull Throwable t) {
                Log.e("MapFragment", "경로 요청 실패: " + t.getMessage(), t);
            }
        });
    }

    // 도착 여부 확인 및 택시 호출 (MapFragment에서도 동일 로직 적용)
    private void checkArrivalInMap(Location location) {
        // isMatched가 true일 때만 도착 확인 로직을 실행하도록 변경
        if (!isMatched || destination == null || matchedUserIds == null || matchedUserIds.isEmpty()) {
            Log.d("CheckArrival", "MapFragment: 매칭 완료 상태가 아니므로 도착 확인 로직을 건너뜝니다.");
            return;
        }

        float[] result = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                destination.latitude, destination.longitude, result);

        Log.d("CheckArrival", "MapFragment 기준 현재 거리: " + result[0] + " meters");

        if (result[0] <= ARRIVAL_RADIUS_METERS) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            // 현재 사용자의 'arrived' 상태를 먼저 확인
            db.collection("users").document(myUid).get().addOnSuccessListener(userDoc -> {
                Boolean alreadyArrived = userDoc.getBoolean("arrived");
                if (alreadyArrived != null && alreadyArrived) {
                    // 이미 도착 처리된 상태이면 중복 호출 방지
                    Log.d("CheckArrival", myUid + "은(는) 이미 도착 처리되었습니다. 중복 호출 방지.");
                    return;
                }

                // 현재 사용자의 arrived 상태를 true로 업데이트
                db.collection("users").document(myUid).update("arrived", true)
                        .addOnSuccessListener(aVoid -> Log.d("CheckArrival", myUid + " arrived 상태 true로 업데이트 완료."))
                        .addOnFailureListener(e -> Log.e("CheckArrival", myUid + " arrived 상태 업데이트 실패: " + e.getMessage()));

                // 모든 파티원들의 도착 상태를 다시 확인
                db.collection("users").whereIn(FieldPath.documentId(), matchedUserIds)
                        .get().addOnSuccessListener(snapshot -> {
                            int arrivedCount = 0;
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                Boolean arrived = doc.getBoolean("arrived");
                                if (arrived != null && arrived) arrivedCount++;
                            }
                            Log.d("CheckArrival", "현재 도착한 파티원 수: " + arrivedCount + "/" + matchedUserIds.size());

                            if (arrivedCount == 1) { // 첫 번째 도착자만 택시 호출 제안 및 알림
                                // 사용자에게 알림을 보냅니다.
                                sendArrivalNotification(destinationName);

                                // 사용자에게 확인 대화 상자 띄우기
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("도착 안내")
                                        .setMessage("출발지에 도착했습니다! 카카오택시 앱을 실행하시겠습니까?")
                                        .setPositiveButton("예", (dialog, which) -> {
                                            launchKakaoTaxiApp(); // 택시 앱 실행 함수 호출
                                        })
                                        .setNegativeButton("아니요", (dialog, which) -> {
                                            // 사용자가 '아니요'를 선택하면, 본인의 'arrived' 상태를 다시 삭제하여 다른 사람이 호출할 수 있게 함
                                            db.collection("users").document(myUid).update("arrived", FieldValue.delete())
                                                    .addOnSuccessListener(aVoid -> Log.d("CheckArrival", myUid + " 도착 상태를 취소했습니다."))
                                                    .addOnFailureListener(e -> Log.e("CheckArrival", myUid + " 도착 상태 취소 실패: " + e.getMessage()));
                                            Toast.makeText(getContext(), "카카오택시 앱 실행을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                        })
                                        .setCancelable(false) // 사용자가 백버튼으로 닫지 못하게
                                        .show();
                            }

                            if (arrivedCount == matchedUserIds.size()) {
                                // 모든 멤버 도착 시 처리
                                Toast.makeText(getContext(), "모든 파티원이 출발지에 도착했습니다!", Toast.LENGTH_LONG).show();
                                // 이 시점에서 MapFragment에서 어떤 화면으로 전환할지 결정 필요
                                // 예: 홈 프래그먼트로 돌아가 매칭 초기화
                                Fragment homeFragment = new HomeFragment();
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, homeFragment)
                                        .commit();

                                // 모든 파티원의 arrived 상태 초기화
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    db.collection("users").document(doc.getId()).update("arrived", FieldValue.delete())
                                            .addOnSuccessListener(aVoid -> Log.d("CheckArrival", doc.getId() + " arrived 상태 초기화 완료."))
                                            .addOnFailureListener(e -> Log.e("CheckArrival", doc.getId() + " arrived 상태 초기화 실패: " + e.getMessage()));
                                }
                            }
                        });
            });
        }
    }

    // 알림을 보내는 메서드 (MapFragment용)
    private void sendArrivalNotification(String originName) {
        Intent intent = new Intent(requireContext(), MainActivity.class); // MainActivity로 돌아가도록 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID_MAP) // MapFragment용 채널 ID 사용
                .setSmallIcon(R.mipmap.ic_launcher) // 앱 아이콘 설정 (AndroidManifest.xml의 ic_launcher 경로 확인)
                .setContentTitle("출발지 도착!")
                .setContentText(originName + "에 도착했습니다. 카카오택시를 호출할까요?")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 높음
                .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 인텐트
                .setAutoCancel(true); // 알림 클릭 시 자동으로 사라지게

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID_MAP, builder.build()); // MapFragment용 알림 ID 사용
        }
    }

    // 카카오택시 앱 실행 로직을 별도 함수로 분리
    private void launchKakaoTaxiApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("kakaot://launch/taxi"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "카카오택시 앱 실행 중 오류가 발생했습니다. Play 스토어로 이동합니다.", Toast.LENGTH_LONG).show();
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kakao.taxi"));
            startActivity(playStoreIntent);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d("MapFragment", "MapFragment에서 지속적인 위치 업데이트 중지.");
        }
        if (mapView != null) {
            mapView.pause(); // 지도 뷰 일시정지 (destroy는 아니므로)
        }
    }
}