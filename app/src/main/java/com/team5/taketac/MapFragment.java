package com.team5.taketac;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.LatLngBounds;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraAnimation;
import com.kakao.vectormap.camera.CameraUpdate;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelManager;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.route.RouteLine;
import com.kakao.vectormap.route.RouteLineLayer;
import com.kakao.vectormap.route.RouteLineManager;
import com.kakao.vectormap.route.RouteLineOptions;
import com.kakao.vectormap.route.RouteLineSegment;
import com.kakao.vectormap.route.RouteLineStyle;
import com.kakao.vectormap.route.RouteLineStyles;

import com.kakao.vectormap.shape.MapPoints;
import com.team5.taketac.api.KakaoDirectionsService;
import com.team5.taketac.model.KakaoDirectionsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private MapView mapView;
    private KakaoMap kakaoMap;
    private LatLng currentLocation;
    private LatLng destination;
    private String destinationName;

    private FusedLocationProviderClient fusedLocationClient;
    private KakaoDirectionsService directionsService;
    private LabelManager labelManager;
    private RouteLineManager routeLineManager;
    private RouteLine currentRouteLine;



    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                          @Nullable android.view.ViewGroup container,
                                          @Nullable Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.map_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.KAKAO_DIRECTIONS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        directionsService = retrofit.create(KakaoDirectionsService.class);


        // 출발지 키 받기
        Bundle args = getArguments();
        if (args != null && args.containsKey("origin_latitude") && args.containsKey("origin_longitude")) {
            double lat = args.getDouble("origin_latitude");
            double lon = args.getDouble("origin_longitude");
            destination = LatLng.from(lat, lon);
            destinationName = args.getString("origin_name", "목적지");  // 기본값으로 "목적지"
        } else {
            // fallback
            destination = Constants.STATIONS.get("가천대역");
            destinationName = "가천대역";
        }



        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() { }

            @Override
            public void onMapError(Exception error) {
                Log.e(TAG, "지도 에러: " + error.getMessage());
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                labelManager = kakaoMap.getLabelManager();
                routeLineManager = kakaoMap.getRouteLineManager();
                requestLocationPermissionAndGet();
            }
        });

        return view;
    }

    private void requestLocationPermissionAndGet() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(getContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = LatLng.from(location.getLatitude(), location.getLongitude());
                Log.d("MapFragment", "현재 위치 받음: " + currentLocation);

                addMarker(currentLocation, "내 위치");
                addMarker(destination, destinationName);
                findRoute(currentLocation, destination);
            } else {
                Toast.makeText(getContext(), "위치를 찾을 수 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("MapFragment", "위치 요청 실패(getCurrentLocation): " + e.getMessage());
            Toast.makeText(getContext(), "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }



    private void addMarker(LatLng position, String tag) {
        try {
            if (labelManager == null) {
                Log.e("addMarker", "labelManager is null");
                return;
            }

            LabelLayer layer = labelManager.getLayer();
            if (layer == null) {
                Log.e("addMarker", "LabelLayer is null");
                return;
            }

            LabelStyle style = LabelStyle.from(R.drawable.ic_launcher_foreground);
            LabelOptions options = LabelOptions.from(position)
                    .setTag(tag)
                    .setStyles(LabelStyles.from(style));

            layer.addLabel(options);
            Log.d("addMarker", "Marker added at: " + position.toString() + " with tag: " + tag);

        } catch (Exception e) {
            Log.e("addMarker", "Error while adding marker", e);
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
                            RouteLineStyle style = RouteLineStyle.from(
                                    10,                      // 선 두께
                                    0xFF007AFF,              // 선 색 (파란색)
                                    3,                       // 외곽선 두께
                                    0xFF000000               // 외곽선 색 (검정)
                            );

                            RouteLineOptions options = RouteLineOptions.from(
                                    RouteLineSegment.from(points, RouteLineStyles.from(style))
                            );

                            // 경로 레이어에서 이전 경로 제거
                            RouteLineLayer routeLineLayer = routeLineManager.getLayer();
                            if (currentRouteLine != null) {
                                routeLineLayer.remove(currentRouteLine);
                            }

                            // 새 경로 추가 및 보관
                            currentRouteLine = routeLineLayer.addRouteLine(options);

                            // --- 카메라 이동 (fitBounds) ---
                            LatLng[] pointArray = points.toArray(new LatLng[0]);
                            CameraUpdate update = CameraUpdateFactory.fitMapPoints(pointArray, 100);
                            kakaoMap.moveCamera(update, CameraAnimation.from(500));
                        }
                    }
                } else {
                    Log.e(TAG, "API 응답 오류: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<KakaoDirectionsResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "길찾기 실패: " + t.getMessage());
            }
        });
    }
}
