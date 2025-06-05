package com.team5.taketac;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PartyFragment extends Fragment {

    public PartyFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // activity_party.xml을 fragment_party.xml로 이름 바꾸었다면 그에 맞게 수정
        return inflater.inflate(R.layout.fragment_party, container, false);
    }
}


