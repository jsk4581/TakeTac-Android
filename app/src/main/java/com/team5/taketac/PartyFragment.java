package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class PartyFragment extends Fragment {

    private RecyclerView recyclerView;
    private PublicPartyAdapter adapter;
    private List<PartyRoom> partyList = new ArrayList<>();
    private DatabaseReference partyRef;
    private TextView tvEmptyMessage;

    public PartyFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_party, container, false);

        recyclerView = view.findViewById(R.id.rvParties);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        FloatingActionButton fabCreate = view.findViewById(R.id.fabCreate);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PublicPartyAdapter(partyList);
        recyclerView.setAdapter(adapter);

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePublicPartyActivity.class);
            startActivity(intent);
        });

        partyRef = FirebaseDatabase.getInstance().getReference("partyRooms");
        loadPartyRooms();

        return view;
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

                if (partyList.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PartyFragment", "DB 에러", error.toException());
            }
        });
    }
}