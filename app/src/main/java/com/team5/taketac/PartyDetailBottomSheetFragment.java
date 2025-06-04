package com.team5.taketac;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class PartyDetailBottomSheetFragment extends BottomSheetDialogFragment {

    private PublicParty publicParty;

    public PartyDetailBottomSheetFragment(PublicParty party) {
        this.publicParty = party;
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

        try {
            // 기본 정보 표시
            tvTitle.setText(publicParty.getTitle() != null ? publicParty.getTitle() : "제목 없음");
            tvLocation.setText(publicParty.getLocation() != null ? publicParty.getLocation() : "장소 없음");

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String creatorId = publicParty.getCreatorId();
            String docId = publicParty.getId();

            Log.d("PartyDetail", "currentUid = " + currentUserId);
            Log.d("PartyDetail", "creatorId = " + creatorId);
            Log.d("PartyDetail", "docId = " + docId);

            // 삭제 버튼 표시 여부
            if (creatorId != null && creatorId.equals(currentUserId)) {
                btnDelete.setVisibility(View.VISIBLE);

                btnDelete.setOnClickListener(v1 -> {
                    if (docId == null || docId.isEmpty()) {
                        Toast.makeText(getContext(), "문서 ID가 없습니다. 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("publicParties")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("PartyDetail", "삭제 실패", e);
                                Toast.makeText(getContext(), "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
            } else {
                btnDelete.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e("PartyDetail", "예외 발생", e);
            Toast.makeText(getContext(), "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            dismiss();  // 안전하게 닫기
        }

        // 참여 버튼은 나중에 구현 예정
        btnJoin.setOnClickListener(v -> {
            Toast.makeText(getContext(), "참여 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show();
        });
    }
}
