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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.team5.taketac.model.PartyRoom;

public class PartyDetailBottomSheetFragment extends BottomSheetDialogFragment {

    private PartyRoom party;

    public PartyDetailBottomSheetFragment(PartyRoom party) {
        this.party = party;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_party_detail_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvTitle = view.findViewById(R.id.tvPartyTitle);
        TextView tvLocation = view.findViewById(R.id.tvPartyLocation);
        Button btnJoin = view.findViewById(R.id.btnJoinParty);
        Button btnDelete = view.findViewById(R.id.btnDeleteParty);

        tvTitle.setText(party.getTitle());
        tvLocation.setText(party.getLocation());

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔐 생성자만 삭제 버튼 보이기
        if (party.getCreatorUid() != null && party.getCreatorUid().equals(currentUserId)) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                FirebaseDatabase.getInstance()
                        .getReference("partyRooms")
                        .child(party.getId())
                        .removeValue()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // 💬 채팅방으로 이동 (파티 ID = 채팅방 ID)
        btnJoin.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chatRoomId", party.getId());
            intent.putExtra("chatRoomName", party.getTitle());
            startActivity(intent);
        });
    }
}
