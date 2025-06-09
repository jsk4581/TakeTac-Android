package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team5.taketac.adapter.ParticipantAdapter;
import com.team5.taketac.model.PartyRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class PartyDetailBottomSheetFragment extends BottomSheetDialogFragment {

    private PartyRoom party;

    public PartyDetailBottomSheetFragment() {}

    public PartyDetailBottomSheetFragment(PartyRoom party) {
        this.party = party;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_party_detail_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        TextView tvTitle = view.findViewById(R.id.tvPartyTitle);
        TextView tvLocation = view.findViewById(R.id.tvPartyLocation);
        TextView tvTime = view.findViewById(R.id.tvPartyTime);
        Button btnJoin = view.findViewById(R.id.btnJoinParty);
        Button btnDelete = view.findViewById(R.id.btnDeleteParty);
        Button btnEdit = view.findViewById(R.id.btnEditParty);
        Button btnChat = view.findViewById(R.id.btnChat);
        RecyclerView rvParticipants = view.findViewById(R.id.rvParticipants);

        tvTitle.setText(party.getTitle());
        tvLocation.setText(party.getLocation());
        tvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
                .format(new Date(party.getTimestamp())));

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference partyRef = FirebaseDatabase.getInstance()
                .getReference("partyRooms").child(party.getId());
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        boolean isHost = currentUserId.equals(party.getCreatorUid());

        if (isHost) {
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            btnJoin.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            btnJoin.setVisibility(View.VISIBLE);
        }

        // ✅ 실시간 참여자 리스트 반영 (비동기 닉네임 완료 후 갱신)
        List<String> nicknameList = new ArrayList<>();
        ParticipantAdapter participantAdapter = new ParticipantAdapter(nicknameList);
        rvParticipants.setAdapter(participantAdapter);
        rvParticipants.setLayoutManager(new LinearLayoutManager(getContext()));

        partyRef.child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nicknameList.clear();

                List<String> tempList = new ArrayList<>();
                AtomicInteger loadedCount = new AtomicInteger(0);
                int totalCount = (int) snapshot.getChildrenCount();

                if (totalCount == 0) {
                    participantAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    userRef.child(uid).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot nickSnap) {
                            String nickname = nickSnap.getValue(String.class);
                            if (nickname != null) {
                                tempList.add(nickname);
                            }
                            if (loadedCount.incrementAndGet() == totalCount) {
                                nicknameList.clear();
                                nicknameList.addAll(tempList);
                                participantAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 초기 참여 여부 UI
        if (!isHost) {
            partyRef.child("participants").child(currentUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isParticipating = snapshot.exists();
                            btnJoin.setText(isParticipating ? "퇴장하기" : "참여하기");
                            btnChat.setVisibility(isParticipating ? View.VISIBLE : View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

            btnJoin.setOnClickListener(v -> {
                partyRef.child("participants").child(currentUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean isParticipating = snapshot.exists();
                                if (isParticipating) {
                                    partyRef.child("participants").child(currentUserId).removeValue();
                                    btnJoin.setText("참여하기");
                                    btnChat.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "퇴장했습니다", Toast.LENGTH_SHORT).show();
                                } else {
                                    partyRef.child("participants").child(currentUserId).setValue(true);
                                    btnJoin.setText("퇴장하기");
                                    btnChat.setVisibility(View.VISIBLE);
                                    Toast.makeText(getContext(), "참여했습니다", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            });
        }

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatRoomId", party.getId());
            intent.putExtra("chatRoomName", party.getTitle());
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            partyRef.removeValue()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "방 삭제 완료", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
        });

        btnEdit.setOnClickListener(v -> {
            EditPartyDialogFragment dialog = new EditPartyDialogFragment(party);
            dialog.setOnPartyUpdatedListener((newTitle, newLocation, newTimestamp) -> {
                party.setTitle(newTitle);
                party.setLocation(newLocation);
                party.setTimestamp(newTimestamp);
                tvTitle.setText(newTitle);
                tvLocation.setText(newLocation);
                tvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
                        .format(new Date(newTimestamp)));
            });
            dialog.show(getParentFragmentManager(), "EditPartyDialog");
        });
    }
}
