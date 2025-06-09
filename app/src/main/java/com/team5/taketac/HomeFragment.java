package com.team5.taketac;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.kakao.vectormap.LatLng;
import com.team5.taketac.adapter.ChatRoomAdapter;
import com.team5.taketac.model.ChatRoomInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;



public class HomeFragment extends Fragment {

    private Spinner spinnerOrigin, spinnerDestination;
    private Button btnRequestMatch, btnViewMap, btnCancelMatch;
    private TextView textViewCount, textChatTitle;
    private LinearLayout layoutMatch, layoutWaiting;
    private RecyclerView recyclerChatRooms;
    private ChatRoomAdapter chatRoomAdapter;

    private String selectedOriginName;
    private FusedLocationProviderClient fusedLocationClient;
    private Handler locationHandler = new Handler();
    private Runnable locationRunnable;
    private static final int LOCATION_INTERVAL = 10000;
    private static final double ARRIVAL_RADIUS_METERS = 50.0;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ListenerRegistration matchListener;
    private Handler matchDelayHandler = new Handler();
    private Runnable delayedMatchRunnable;

    private boolean isMatched = false;
    private boolean hasMatched = false;
    private List<String> currentPartyMembers = new ArrayList<>();

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
        textChatTitle = view.findViewById(R.id.text_chat_title);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRoomAdapter = new ChatRoomAdapter(new ArrayList<>(), requireContext(), room -> {
            ChatRoomBottomSheetFragment bottomSheet = new ChatRoomBottomSheetFragment(
                    room.getId(),
                    String.join(", ", room.getNicknames()),
                    this::loadChatRooms
            );
            bottomSheet.show(getParentFragmentManager(), "ChatRoomBottomSheet");
        });
        recyclerChatRooms.setAdapter(chatRoomAdapter);

        if (MatchState.isMatched || MatchState.isWaiting) {
            isMatched = MatchState.isMatched;
            selectedOriginName = MatchState.origin;
            currentPartyMembers = new ArrayList<>(MatchState.partyMembers);
            layoutMatch.setVisibility(View.GONE);
            layoutWaiting.setVisibility(View.VISIBLE);
        } else {
            layoutMatch.setVisibility(View.VISIBLE);
            layoutWaiting.setVisibility(View.GONE);
        }

        setupSpinners();
        startLocationUpdates();
        loadChatRooms();

        btnRequestMatch.setOnClickListener(v -> {
            selectedOriginName = (String) spinnerOrigin.getSelectedItem();
            if (currentUser == null) return;

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
                        MatchState.isWaiting = true;
                        MatchState.origin = selectedOriginName;
                        MatchState.partyMembers = new ArrayList<>(List.of(currentUser.getEmail()));
                        listenForMatching();
                    });
        });

        btnCancelMatch.setOnClickListener(v -> {
            if (currentUser != null) {
                db.collection("match_requests").document(currentUser.getEmail()).delete();
                layoutMatch.setVisibility(View.VISIBLE);
                layoutWaiting.setVisibility(View.GONE);
                if (matchListener != null) matchListener.remove();
                if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
                isMatched = false;
                hasMatched = false;
                currentPartyMembers.clear();
                MatchState.isMatched = false;
                MatchState.isWaiting = false;
                MatchState.origin = null;
                MatchState.partyMembers.clear();
            }
        });

        btnViewMap.setOnClickListener(v -> {
            if (selectedOriginName == null || currentUser == null) {
                Toast.makeText(getContext(), "출발지를 선택하고 매칭을 시작하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> showList = new ArrayList<>(currentPartyMembers);
            if (!showList.contains(currentUser.getEmail())) {
                showList.add(currentUser.getEmail());
            }
            showMapFragment(showList);
        });

        return view;
    }

    private void loadChatRooms() {
        if (currentUser == null) return;

        db.collection("parties")
                .whereArrayContains("members", currentUser.getEmail())
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null || snapshot.isEmpty()) {
                        chatRoomAdapter.setRooms(new ArrayList<>());
                        return;
                    }

                    List<ChatRoomInfo> roomInfoList = new ArrayList<>();
                    List<DocumentSnapshot> partyDocs = snapshot.getDocuments();
                    int total = partyDocs.size();
                    AtomicInteger count = new AtomicInteger(0);

                    for (DocumentSnapshot doc : partyDocs) {
                        List<String> memberEmails = (List<String>) doc.get("members");

                        db.collection("users")
                                .whereIn(FieldPath.documentId(), memberEmails)
                                .get()
                                .addOnSuccessListener(userSnapshot -> {
                                    List<String> nicknames = new ArrayList<>();
                                    for (DocumentSnapshot userDoc : userSnapshot.getDocuments()) {
                                        String nickname = userDoc.getString("nickname");
                                        if (nickname != null) nicknames.add(nickname);
                                    }

                                    db.collection("users").document(currentUser.getEmail())
                                            .get()
                                            .addOnSuccessListener(currentUserDoc -> {
                                                String myNick = currentUserDoc.getString("nickname");
                                                if (myNick != null && nicknames.remove(myNick)) {
                                                    nicknames.add(0, myNick);
                                                }

                                                roomInfoList.add(new ChatRoomInfo(doc.getId(), nicknames));

                                                if (count.incrementAndGet() == total) {
                                                    chatRoomAdapter.setRooms(roomInfoList);
                                                }
                                            });
                                });
                    }
                });
    }

    private void listenForMatching() {
        if (currentUser == null) return;
        if (matchListener != null) matchListener.remove();

        matchListener = db.collection("match_requests")
                .whereEqualTo("origin", selectedOriginName)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null || snapshot.isEmpty()) return;

                    List<String> userIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        userIds.add(doc.getId());
                    }

                    textViewCount.setText("현재 대기 인원: " + userIds.size() + "/4");

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
        if (hasMatched) return;
        if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
        delayedMatchRunnable = () -> matchConfirmed(userIds);
        matchDelayHandler.postDelayed(delayedMatchRunnable, delayMillis);
    }

    private void runMatchingImmediately(List<String> userIds) {
        if (hasMatched) return;
        if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
        matchConfirmed(userIds);
    }

    private void matchConfirmed(List<String> matchedUserIds) {
        // ✅ 중복 방지용 정렬 및 대표 판단
        Collections.sort(matchedUserIds);
        if (!matchedUserIds.get(0).equals(currentUser.getEmail())) return;

        // ✅ 중복 생성 방지 (이미 파티 생성된 경우 체크)
        db.collection("parties")
                .whereEqualTo("origin", selectedOriginName)
                .whereEqualTo("members", matchedUserIds)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Log.d("HomeFragment", "이미 생성된 파티 존재함");
                        return;
                    }

                    Map<String, Object> party = new HashMap<>();
                    party.put("origin", selectedOriginName);
                    party.put("timestamp", FieldValue.serverTimestamp());
                    party.put("members", matchedUserIds);

                    db.collection("parties")
                            .add(party)
                            .addOnSuccessListener(docRef -> Log.d("HomeFragment", "파티 저장 완료: " + docRef.getId()));

                    for (String uid : matchedUserIds) {
                        db.collection("match_requests").document(uid).delete();
                    }

                    if (matchListener != null) matchListener.remove();
                    isMatched = true;
                    currentPartyMembers = matchedUserIds;
                    MatchState.isMatched = true;
                    MatchState.origin = selectedOriginName;
                    MatchState.partyMembers = new ArrayList<>(matchedUserIds);
                });
    }


    private void showMapFragment(List<String> matchedUserIds) {
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
        bundle.putStringArrayList("matched_user_ids", new ArrayList<>(matchedUserIds));
        mapFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
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
        locationRunnable = () -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && currentUser != null) {
                    Map<String, Object> loc = new HashMap<>();
                    loc.put("latitude", location.getLatitude());
                    loc.put("longitude", location.getLongitude());
                    loc.put("timestamp", FieldValue.serverTimestamp());
                    db.collection("users")
                            .document(currentUser.getEmail())
                            .collection("location")
                            .document("current")
                            .set(loc, SetOptions.merge());
                    checkArrival(location);
                }
            });
            locationHandler.postDelayed(locationRunnable, LOCATION_INTERVAL);
        };
        locationHandler.post(locationRunnable);
    }

    private void checkArrival(Location location) {
        if (!isMatched || selectedOriginName == null || currentPartyMembers.isEmpty()) return;
        LatLng origin = Constants.STATIONS.get(selectedOriginName);
        if (origin == null) return;

        float[] result = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), origin.latitude, origin.longitude, result);
        if (result[0] <= ARRIVAL_RADIUS_METERS) {
            db.collection("users").document(currentUser.getEmail()).update("arrived", true);
            db.collection("users").whereIn(FieldPath.documentId(), currentPartyMembers).get().addOnSuccessListener(snapshot -> {
                boolean allArrived = true;
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Boolean arrived = doc.getBoolean("arrived");
                    if (arrived == null || !arrived) {
                        allArrived = false;
                        break;
                    }
                }
                if (allArrived) {
                    layoutMatch.setVisibility(View.VISIBLE);
                    layoutWaiting.setVisibility(View.GONE);
                    isMatched = false;
                    hasMatched = false;
                    currentPartyMembers.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        db.collection("users").document(doc.getId()).update("arrived", FieldValue.delete());
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationHandler != null && locationRunnable != null) locationHandler.removeCallbacks(locationRunnable);
        if (matchListener != null) matchListener.remove();
        if (delayedMatchRunnable != null) matchDelayHandler.removeCallbacks(delayedMatchRunnable);
    }
}
