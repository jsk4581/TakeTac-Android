package com.team5.taketac;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;
import com.team5.taketac.model.PartyRoom;

import java.util.Calendar;

public class EditPartyDialogFragment extends DialogFragment {

    private PartyRoom party;
    private OnPartyUpdatedListener listener;

    private TextInputEditText etTitle, etLocation;
    private Button btnDate, btnTime, btnUpdate;

    private int year, month, day, hour, minute;

    public EditPartyDialogFragment(PartyRoom party) {
        this.party = party;
    }

    public void setOnPartyUpdatedListener(OnPartyUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_party, container, false);

        etTitle = view.findViewById(R.id.etTitle);
        etLocation = view.findViewById(R.id.etLocation);
        btnDate = view.findViewById(R.id.btnDate);
        btnTime = view.findViewById(R.id.btnTime);
        btnUpdate = view.findViewById(R.id.btnUpdate);

        // 초기화
        etTitle.setText(party.getTitle());
        etLocation.setText(party.getLocation());

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(party.getTimestamp());
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);

        updateDateText();
        updateTimeText();

        btnDate.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (DatePicker dp, int y, int m, int d) -> {
                year = y; month = m; day = d;
                updateDateText();
            }, year, month, day).show();
        });

        btnTime.setOnClickListener(v -> {
            new TimePickerDialog(getContext(), (TimePicker tp, int h, int min) -> {
                hour = h; minute = min;
                updateTimeText();
            }, hour, minute, true).show();
        });

        btnUpdate.setOnClickListener(v -> {
            String newTitle = etTitle.getText().toString().trim();
            String newLocation = etLocation.getText().toString().trim();

            if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newLocation)) {
                Toast.makeText(getContext(), "제목과 장소를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar updatedCal = Calendar.getInstance();
            updatedCal.set(year, month, day, hour, minute, 0);
            long newTimestamp = updatedCal.getTimeInMillis();

            FirebaseDatabase.getInstance()
                    .getReference("partyRooms")
                    .child(party.getId())
                    .child("title").setValue(newTitle);
            FirebaseDatabase.getInstance()
                    .getReference("partyRooms")
                    .child(party.getId())
                    .child("location").setValue(newLocation);
            FirebaseDatabase.getInstance()
                    .getReference("partyRooms")
                    .child(party.getId())
                    .child("timestamp").setValue(newTimestamp);

            if (listener != null) {
                listener.onPartyUpdated(newTitle, newLocation, newTimestamp);
            }

            Toast.makeText(getContext(), "수정 완료", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    private void updateDateText() {
        btnDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day));
    }

    private void updateTimeText() {
        btnTime.setText(String.format("%02d:%02d", hour, minute));
    }
}
