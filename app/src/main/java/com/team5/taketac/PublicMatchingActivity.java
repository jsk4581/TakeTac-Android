package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PublicMatchingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PublicPartyAdapter adapter;
    private List<PublicParty> partyList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "PublicMatching";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 로그인 안 되어 있으면 로그인 화면으로
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_public_matching);

        recyclerView = findViewById(R.id.rvParties);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicPartyAdapter(this, partyList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        fabCreate.setOnClickListener(v -> {
            Log.d(TAG, "FAB 클릭됨");
            Intent intent = new Intent(PublicMatchingActivity.this, CreatePublicPartyActivity.class);
            startActivity(intent);
        });

        loadPublicParties();
    }

    private void loadPublicParties() {
        db.collection("publicParties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    partyList.clear();

                    Log.d(TAG, "총 문서 수: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            PublicParty party = doc.toObject(PublicParty.class);
                            party.setId(doc.getId());

                            Log.d(TAG, "title = " + party.getTitle() +
                                    ", location = " + party.getLocation() +
                                    ", date = " + party.getDate() +
                                    ", time = " + party.getTime());

                            partyList.add(party);
                        } catch (Exception e) {
                            Log.e(TAG, "문서 파싱 실패: " + doc.getId(), e);
                        }
                    }

                    Log.d(TAG, "리스트에 추가된 파티 수: " + partyList.size());
                    adapter.notifyDataSetChanged();

                    if (partyList.isEmpty()) {
                        Toast.makeText(this, "등록된 공개 파티가 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "파티 불러오기 실패", e);
                    Toast.makeText(this, "파티 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
