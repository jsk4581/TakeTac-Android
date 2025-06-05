package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team5.taketac.adapter.PublicPartyAdapter;
import com.team5.taketac.model.PartyRoom;

import java.util.ArrayList;
import java.util.List;

public class PublicMatchingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PublicPartyAdapter adapter;
    private List<PartyRoom> partyList = new ArrayList<>();
    private DatabaseReference partyRef;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_matching);

        recyclerView = findViewById(R.id.rvParties);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicPartyAdapter(partyList);
        recyclerView.setAdapter(adapter);

        tvEmptyMessage = findViewById(R.id.tvEmptyMessage); // 🎯 레이아웃에 추가 필요

        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(PublicMatchingActivity.this, CreatePublicPartyActivity.class);
            startActivity(intent);
        });

        partyRef = FirebaseDatabase.getInstance().getReference("partyRooms");
        loadPartyRooms();
    }

    private void loadPartyRooms() {
        partyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                partyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PartyRoom party = dataSnapshot.getValue(PartyRoom.class);
                    if (party != null) {
                        partyList.add(party);
                    }
                }
                adapter.notifyDataSetChanged();

                // 👇 빈 목록 메시지 표시
                if (partyList.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PublicMatching", "DB 에러", error.toException());
            }
        });
    }
}
