package com.team5.taketac;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.kakao.vectormap.LatLng;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private Spinner spinnerOrigin;
    private Spinner spinnerDestination;
    private Button btnRequestMatch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        spinnerOrigin = view.findViewById(R.id.spinner_origin);
        spinnerDestination = view.findViewById(R.id.spinner_destination);
        btnRequestMatch = view.findViewById(R.id.btn_request_match);

        setupSpinners();

        btnRequestMatch.setOnClickListener(v -> {
            String selectedOriginName = (String) spinnerOrigin.getSelectedItem();

            LatLng originCoords = Constants.STATIONS.get(selectedOriginName);

            if (originCoords != null) {
                MapFragment mapFragment = new MapFragment();

                Bundle bundle = new Bundle();
                bundle.putDouble("origin_latitude", originCoords.latitude);
                bundle.putDouble("origin_longitude", originCoords.longitude);
                bundle.putString("origin_name", selectedOriginName);

                mapFragment.setArguments(bundle);

                FragmentTransaction transaction = requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();

                transaction.replace(R.id.fragment_container, mapFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                Toast.makeText(getContext(), "출발지 좌표를 찾을 수 없습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setupSpinners() {
        List<String> stationNames = new ArrayList<>(Constants.STATIONS.keySet());
        ArrayAdapter<String> originAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, stationNames);
        originAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrigin.setAdapter(originAdapter);

        List<String> buildingNames = new ArrayList<>(Constants.BUILDINGS.keySet());
        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, buildingNames);
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDestination.setAdapter(destinationAdapter);
    }
}
