package com.team5.taketac;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.kakao.vectormap.LatLng;
import java.util.*;
import com.team5.taketac.adapter.ChatRoomAdapter;
import com.team5.taketac.model.ChatRoomInfo;
import com.team5.taketac.model.PartyRoom;

public class HomeFragment extends Fragment {

    private Spinner spinnerOrigin, spinnerDestination;
    private Button btnRequestMatch, btnViewMap, btnCancelMatch;
    private TextView textViewCount, textChatTitle;
    private LinearLayout layoutMatch, layoutWaiting;
    private RecyclerView recyclerChatRooms;
    private ChatRoomAdapter chatRoomAdapter;

    private String selectedOriginName;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private static final int LOCATION_INTERVAL = 10000;
    private static final int FASTEST_LOCATION_INTERVAL = 5000;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1002;

    private static final double ARRIVAL_RADIUS_METERS = 100.0;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ListenerRegistration matchListener;
    private Handler matchDelayHandler = new Handler();
    private Runnable delayedMatchRunnable;

    private boolean isMatched = false;
    private List<String> currentPartyMembers = new ArrayList<>();
    // 매칭 요청 대기 중인 모든 사용자 ID를 추적할 리스트 추가
    private List<String> currentWaitingUsers = new ArrayList<>();


    // Notification 관련 상수 추가
    private static final String CHANNEL_ID = "take_tac_arrival_channel";
    private static final int NOTIFICATION_ID = 101;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        layoutMatch = view.findViewById(R.id.layout_match);
        layoutWaiting = view.findViewById(R.id.layout_waiting);
        spinnerOrigin = view.findViewById(R.id.spinner_origin);
        spinnerDestination = view.findViewById(R.id.spinner_destination);
        btnRequestMatch = view.findViewById(R.id.btn_request_match);
        btnViewMap = view.findViewById(R.id.btn_show_map);
        btnCancelMatch = view.findViewById(R.id.btn_cancel_match);
        textViewCount = view.findViewById(R.id.text_view_count);
        recyclerChatRooms = view.findViewById(R.id.recycler_chatrooms);
        recyclerChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRoomAdapter = new ChatRoomAdapter(new ArrayList<>(), requireContext());
        recyclerChatRooms.setAdapter(chatRoomAdapter);
        textChatTitle = view.findViewById(R.id.text_chat_title);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (MatchState.isMatched) {
            selectedOriginName = MatchState.origin;
            currentPartyMembers = new ArrayList<>(MatchState.partyMembers);
            layoutMatch.setVisibility(View.GONE);
            layoutWaiting.setVisibility(View.VISIBLE);
            currentWaitingUsers = new ArrayList<>(currentPartyMembers);
        } else {
            layoutMatch.setVisibility(View.VISIBLE);
            layoutWaiting.setVisibility(View.GONE);
        }

        setupSpinners();
        // 알림 채널 생성 (앱 시작 시 한 번만 호출)
        createNotificationChannel();
        // 위치 권한 확인 및 업데이트 시작
        checkAndRequestLocationPermission();
        loadChatRooms();

        btnRequestMatch.setOnClickListener(v -> {
            selectedOriginName = (String) spinnerOrigin.getSelectedItem();
            if (currentUser == null) return;
            if (selectedOriginName == null || selectedOriginName.isEmpty()) {
                Toast.makeText(getContext(), "출발지를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 이전 도착 상태 초기화
            db.collection("users").document(currentUser.getEmail())
                    .update("arrived", FieldValue.delete(), "arrivedTimestamp", FieldValue.delete())
                    .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "매칭 전에 arrived 상태 초기화 완료"))
                    .addOnFailureListener(e -> Log.e("HomeFragment", "arrived 초기화 실패: " + e.getMessage()));



            Map<String, Object> request = new HashMap<>();
            request.put("uid", currentUser.getEmail());
            request.put("origin", selectedOriginName);
            request.put("timestamp", FieldValue.serverTimestamp());

            db.collection("users").document(currentUser.getEmail())
                    .set(Collections.singletonMap("origin", selectedOriginName), SetOptions.merge());

            db.collection("match_requests").document(currentUser.getEmail())
                    .set(request)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "매칭 요청됨", Toast.LENGTH_SHORT).show();
                        layoutMatch.setVisibility(View.GONE);
                        layoutWaiting.setVisibility(View.VISIBLE);
                        listenForMatching(); // 매칭 리스너 시작

                        MatchState.isMatched = false;
                        MatchState.isWaiting = true;
                        MatchState.origin = selectedOriginName;
                        MatchState.partyMembers = new ArrayList<>(List.of(currentUser.getEmail()));
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeFragment", "매칭 요청 실패: " + e.getMessage());
                        Toast.makeText(getContext(), "매칭 요청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancelMatch.setOnClickListener(v -> {
            if (currentUser != null) {
                db.collection("match_requests").document(currentUser.getEmail()).delete();
                // 사용자의 arrived 상태도 함께 초기화 (혹시 모를 상황 대비)
                db.collection("users").document(currentUser.getEmail()).update("arrived", FieldValue.delete())
                        .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "사용자 arrived 상태 초기화 완료"))
                        .addOnFailureListener(e -> Log.e("HomeFragment", "사용자 arrived 상태 초기화 실패: " + e.getMessage()));

                MatchState.isMatched = false;
                MatchState.isWaiting = false;
                MatchState.origin = null;
                MatchState.partyMembers.clear();

                layoutMatch.setVisibility(View.VISIBLE);
                layoutWaiting.setVisibility(View.GONE);
                if (matchListener != null) matchListener.remove();
                if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
                isMatched = false;
                currentPartyMembers.clear();
                currentWaitingUsers.clear(); // 대기 중인 유저 목록도 초기화
            }
        });

        btnViewMap.setOnClickListener(v -> {
            // 매칭 요청을 보낸 후 (즉, layoutWaiting이 보이는 상태) 지도를 볼 수 있도록 합니다.
            if (selectedOriginName == null || currentUser == null) {
                Toast.makeText(getContext(), "출발지를 선택하고 매칭을 요청해야 지도를 볼 수 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }


            // 매칭 여부와 상관없이 현재 대기 중이거나 매칭된 모든 유저를 지도에 표시
            List<String> showList = new ArrayList<>(currentWaitingUsers);
            if (!showList.contains(currentUser.getEmail())) {
                showList.add(currentUser.getEmail());
            }
            showMapFragment(showList);
        });
        return view;
    }

    // 알림 채널 생성 메서드
    private void createNotificationChannel() {
        // Android 8.0 (API 레벨 26) 이상에서만 알림 채널이 필요합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "도착 알림";
            String description = "출발지 도착 시 알림을 받습니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH; // 높은 중요도로 설정
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // 알림 채널을 시스템에 등록
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // 위치 권한 확인 및 요청 메서드
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE_LOCATION);
        } else {
            // 권한이 이미 있다면 위치 업데이트 시작
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여되면 위치 업데이트 시작
                startLocationUpdates();
            } else {
                Toast.makeText(getContext(), "위치 권한이 거부되었습니다. 앱 기능에 제한이 있을 수 있습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadChatRooms() {
        if (currentUser == null) return;

        db.collection("parties")
                .whereArrayContains("members", currentUser.getEmail())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("HomeFragment", "채팅방 로드 실패.", e);
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<ChatRoomInfo> roomInfoList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            List<String> memberEmails = (List<String>) doc.get("members");
                            if (memberEmails == null || memberEmails.isEmpty()) continue;

                            db.collection("users")
                                    .whereIn(FieldPath.documentId(), memberEmails)
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        List<String> nicknames = new ArrayList<>();
                                        for (DocumentSnapshot userDoc : userSnapshot.getDocuments()) {
                                            String nickname = userDoc.getString("nickname");
                                            if (nickname != null && !nickname.isEmpty()) {
                                                nicknames.add(nickname);
                                            }
                                        }

                                        db.collection("users").document(currentUser.getEmail())
                                                .get()
                                                .addOnSuccessListener(currentUserDoc -> {
                                                    String myNick = currentUserDoc.getString("nickname");
                                                    if (myNick != null && !myNick.isEmpty() && nicknames.remove(myNick)) {
                                                        nicknames.add(0, myNick);
                                                    }
                                                    roomInfoList.add(new ChatRoomInfo(doc.getId(), nicknames));
                                                    chatRoomAdapter.setRooms(roomInfoList);
                                                });
                                    })
                                    .addOnFailureListener(userE -> Log.e("HomeFragment", "닉네임 가져오기 실패: ", userE));
                        }
                    } else {
                        chatRoomAdapter.setRooms(new ArrayList<>());
                    }
                });
    }

    private void listenForMatching() {
        if (currentUser == null) return;
        if (matchListener != null) matchListener.remove();

        matchListener = db.collection("match_requests")
                .whereEqualTo("origin", selectedOriginName)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("HomeFragment", "매칭 요청 리스너 실패.", e);
                        return;
                    }
                    if (snapshot == null || snapshot.isEmpty()) {
                        textViewCount.setText("현재 대기 인원: 0/4");
                        currentWaitingUsers.clear(); // 대기 인원 없을 시 초기화
                        return;
                    }

                    List<String> userIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        userIds.add(doc.getId());
                    }
                    currentWaitingUsers = new ArrayList<>(userIds); // 현재 대기 중인 유저 업데이트

                    textViewCount.setText("현재 대기 인원: " + userIds.size() + "/4");

                    // 매칭 로직은 그대로 유지
                    if (userIds.contains(currentUser.getEmail())) {
                        if (userIds.size() == 4) {
                            runMatchingImmediately(userIds);
                        } else if (userIds.size() == 3) {
                            delayMatching(userIds, 30000);
                        } else if (userIds.size() == 2) {
                            delayMatching(userIds, 60000);
                        }
                    }
                });
    }

    private void delayMatching(List<String> userIds, long delayMillis) {
        if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
        delayedMatchRunnable = () -> {
            db.collection("match_requests")
                    .whereEqualTo("origin", selectedOriginName)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<String> usersAfterDelay = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            usersAfterDelay.add(doc.getId());
                        }
                        // 지연 후에도 조건이 맞는지 다시 확인
                        if (usersAfterDelay.contains(currentUser.getEmail()) &&
                                (usersAfterDelay.size() == 2 || usersAfterDelay.size() == 3 || usersAfterDelay.size() == 4)) {
                            matchConfirmed(usersAfterDelay);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("HomeFragment", "지연 매칭 확인 실패", e));
        };
        matchDelayHandler.postDelayed(delayedMatchRunnable, delayMillis);
    }

    private void runMatchingImmediately(List<String> userIds) {
        if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
        matchConfirmed(userIds);
    }

    private void matchConfirmed(List<String> matchedUserIds) {
        if (!matchedUserIds.contains(currentUser.getEmail())) {
            return;
        }

        Map<String, Object> party = new HashMap<>();
        party.put("origin", selectedOriginName);
        party.put("timestamp", FieldValue.serverTimestamp());
        party.put("members", matchedUserIds);

        db.collection("parties")
                .add(party)
                .addOnSuccessListener(docRef -> Log.d("HomeFragment", "파티 저장 완료: " + docRef.getId()))
                .addOnFailureListener(e -> Log.e("HomeFragment", "파티 저장 실패: " + e.getMessage()));


        for (String uid : matchedUserIds) {
            db.collection("match_requests").document(uid).delete()
                    .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "매칭 요청 삭제 완료: " + uid))
                    .addOnFailureListener(e -> Log.e("HomeFragment", "매칭 요청 삭제 실패: " + uid + " " + e.getMessage()));
        }

        if (matchListener != null) matchListener.remove();
        isMatched = true;
        currentPartyMembers = matchedUserIds;
        currentWaitingUsers.clear(); // 매칭 완료 시 대기 유저 목록 초기화

        Toast.makeText(getContext(), "매칭이 완료되었습니다!", Toast.LENGTH_LONG).show();
    }

    private void showMapFragment(List<String> usersToShowOnMap) { // 파라미터 이름 변경
        LatLng originCoords = Constants.STATIONS.get(selectedOriginName);
        if (originCoords == null) {
            Toast.makeText(getContext(), "출발지 좌표를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        MapFragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble("origin_latitude", originCoords.latitude);
        bundle.putDouble("origin_longitude", originCoords.longitude);
        bundle.putString("origin_name", selectedOriginName);
        bundle.putStringArrayList("matched_user_ids", new ArrayList<>(usersToShowOnMap)); // 변경된 파라미터 전달
        mapFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .hide(this)
                .add(R.id.fragment_container, mapFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupSpinners() {
        ArrayAdapter<String> originAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(Constants.STATIONS.keySet()));
        originAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(originAdapter);

        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(Constants.BUILDINGS.keySet()));
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestination.setAdapter(destinationAdapter);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w("HomeFragment", "LocationResult is null.");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null && currentUser != null) {
                        Map<String, Object> loc = new HashMap<>();
                        loc.put("latitude", location.getLatitude());
                        loc.put("longitude", location.getLongitude());
                        loc.put("timestamp", FieldValue.serverTimestamp());
                        db.collection("users")
                                .document(currentUser.getEmail())
                                .collection("location")
                                .document("current")
                                .set(loc, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    // Log.d("HomeFragment", "HomeFragment: 위치 저장 완료: " + location.getLatitude() + ", " + location.getLongitude());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("HomeFragment", "HomeFragment: 위치 저장 실패: " + e.getMessage());
                                });
                        checkArrival(location);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            Log.d("HomeFragment", "HomeFragment에서 지속적인 위치 업데이트 요청 시작.");
        } else {
            Log.w("HomeFragment", "HomeFragment: 위치 권한이 없어 위치 업데이트를 시작할 수 없습니다.");
        }
    }

    private void checkArrival(Location location) {
        // isMatched가 true일 때만 도착 확인 로직을 실행하도록 유지합니다.
        // 매칭 대기 중에는 도착 확인 로직이 불필요합니다.
        if (!isMatched || selectedOriginName == null || currentPartyMembers.isEmpty()) return;

        LatLng origin = Constants.STATIONS.get(selectedOriginName);
        if (origin == null) {
            Log.e("HomeFragment", "선택된 출발지(" + selectedOriginName + ")의 좌표를 찾을 수 없습니다.");
            return;
        }

        float[] result = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), origin.latitude, origin.longitude, result);
        Log.d("CheckArrival", "HomeFragment 기준 현재 거리: " + result[0] + " meters");

        if (result[0] <= ARRIVAL_RADIUS_METERS) {
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            db.collection("users").document(myUid).get().addOnSuccessListener(userDoc -> {
                Boolean alreadyArrived = userDoc.getBoolean("arrived");
                if (alreadyArrived != null && alreadyArrived) {
                    Log.d("CheckArrival", myUid + "은(는) 이미 도착 처리되었습니다. 중복 호출 방지.");
                    return;
                }

                db.collection("users").document(myUid).update("arrived", true)
                        .addOnSuccessListener(aVoid -> Log.d("CheckArrival", myUid + " arrived 상태 true로 업데이트 완료."))
                        .addOnFailureListener(e -> Log.e("CheckArrival", myUid + " arrived 상태 업데이트 실패: " + e.getMessage()));


                db.collection("users").whereIn(FieldPath.documentId(), currentPartyMembers)
                        .get().addOnSuccessListener(snapshot -> {
                            int arrivedCount = 0;
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                Boolean arrived = doc.getBoolean("arrived");
                                if (arrived != null && arrived) arrivedCount++;
                            }
                            Log.d("CheckArrival", "현재 도착한 파티원 수: " + arrivedCount + "/" + currentPartyMembers.size());

                            if (arrivedCount == 1) { // 첫 번째 도착자만 택시 호출 제안
                                // 사용자에게 알림을 보냅니다.
                                sendArrivalNotification(selectedOriginName);

                                // 사용자에게 확인 대화 상자를 띄웁니다.
                                new AlertDialog.Builder(requireContext())
                                        .setTitle("출발지 도착 알림")
                                        .setMessage("출발지에 도착했습니다! 카카오택시 앱을 실행하시겠습니까?")
                                        .setPositiveButton("예", (dialog, which) -> {
                                            launchKakaoTaxiApp();
                                        })
                                        .setNegativeButton("아니요", (dialog, which) -> {
                                            db.collection("users").document(myUid).update("arrived", FieldValue.delete())
                                                    .addOnSuccessListener(aVoid -> Log.d("CheckArrival", myUid + " 도착 상태를 취소했습니다."))
                                                    .addOnFailureListener(e -> Log.e("CheckArrival", myUid + " 도착 상태 취소 실패: " + e.getMessage()));
                                            Toast.makeText(getContext(), "카카오택시 앱 실행을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                        })
                                        .setCancelable(false)
                                        .show();
                            }

                            if (arrivedCount == currentPartyMembers.size()) {
                                Toast.makeText(getContext(), "모든 파티원이 출발지에 도착했습니다!", Toast.LENGTH_LONG).show();
                                layoutMatch.setVisibility(View.VISIBLE);
                                layoutWaiting.setVisibility(View.GONE);
                                isMatched = false;
                                currentPartyMembers.clear();
                                currentWaitingUsers.clear(); // 매칭 완료 시 대기 유저 목록 초기화
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

    // 알림을 보내는 메서드
    private void sendArrivalNotification(String originName) {
        Intent intent = new Intent(requireContext(), MainActivity.class); // MainActivity로 돌아가도록 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // 앱 아이콘 (AndroidManifest.xml의 ic_launcher 경로 확인)
                .setContentTitle("출발지 도착!")
                .setContentText(originName + "에 도착했습니다. 카카오택시를 호출할까요?")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 높음
                .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 인텐트
                .setAutoCancel(true); // 알림 클릭 시 자동으로 사라지게

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void launchKakaoTaxiApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("kakaot://launch/taxi"));
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "카카오택시 앱이 설치되어 있지 않습니다. Play 스토어로 이동합니다.", Toast.LENGTH_LONG).show();
                Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kakao.taxi"));
                startActivity(playStoreIntent);
            }
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
            Log.d("HomeFragment", "HomeFragment에서 지속적인 위치 업데이트 중지.");
        }
        if (matchListener != null) matchListener.remove();
        if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (MatchState.isMatched || MatchState.isWaiting) {
            selectedOriginName = MatchState.origin;
            currentPartyMembers = new ArrayList<>(MatchState.partyMembers);
            layoutMatch.setVisibility(View.GONE);
            layoutWaiting.setVisibility(View.VISIBLE);
            currentWaitingUsers = new ArrayList<>(currentPartyMembers);
        } else {
            layoutMatch.setVisibility(View.VISIBLE);
            layoutWaiting.setVisibility(View.GONE);
        }
    }

}