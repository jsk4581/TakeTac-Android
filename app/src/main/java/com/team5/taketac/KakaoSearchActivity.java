package com.team5.taketac;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.annotations.SerializedName;

import java.util.*;
import java.util.regex.Pattern;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public class KakaoSearchActivity extends AppCompatActivity {

    private EditText editKeyword;
    private Button btnSearch;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> addressList = new ArrayList<>();
    private Map<String, String> addressMap = new HashMap<>();

    private static final String KAKAO_BASE_URL = "https://dapi.kakao.com/";
    private static final String REST_API_KEY = "KakaoAK c14ff2f90a932b0beaf3c05da8fb4a95"; // 본인의 키로 교체 필요

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_search);

        editKeyword = findViewById(R.id.editKeyword);
        btnSearch = findViewById(R.id.btnSearch);
        listView = findViewById(R.id.listViewAddress);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addressList);
        listView.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> {
            String keyword = editKeyword.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchAddress(keyword);
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = addressList.get(position);
            String fullAddress = addressMap.get(selected);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedAddress", fullAddress);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 전달받은 키워드 있을 경우 자동 검색
        String initKeyword = getIntent().getStringExtra("keyword");
        if (initKeyword != null) {
            editKeyword.setText(initKeyword);
            searchAddress(initKeyword);
        }
    }

    private void searchAddress(String keyword) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(KAKAO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        KakaoApiService apiService = retrofit.create(KakaoApiService.class);
        apiService.searchAddress(REST_API_KEY, keyword, 30)
                .enqueue(new Callback<KakaoSearchResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<KakaoSearchResponse> call,
                                           @NonNull Response<KakaoSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            addressList.clear();
                            addressMap.clear();

                            for (KakaoDocument doc : response.body().documents) {
                                String address = doc.address.address_name;

                                // 행정동(읍/면/동)까지만 표시하며, 숫자 포함 여부와 관계없이 필터링
                                if (address.endsWith("동") || address.endsWith("읍") || address.endsWith("면")) {
                                    addressList.add(address);
                                    addressMap.put(address, address);
                                }
                            }

                            if (addressList.isEmpty()) {
                                Toast.makeText(KakaoSearchActivity.this, "검색결과 없음", Toast.LENGTH_SHORT).show();
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(KakaoSearchActivity.this, "검색 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<KakaoSearchResponse> call, @NonNull Throwable t) {
                        Toast.makeText(KakaoSearchActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                        Log.e("KakaoSearch", "API Error", t);
                    }
                });
    }

    interface KakaoApiService {
        @GET("v2/local/search/address.json")
        Call<KakaoSearchResponse> searchAddress(
                @Header("Authorization") String apiKey,
                @Query("query") String query,
                @Query("size") int size
        );
    }

    static class KakaoSearchResponse {
        List<KakaoDocument> documents;
    }

    static class KakaoDocument {
        KakaoAddress address;
    }

    static class KakaoAddress {
        @SerializedName("address_name")
        String address_name;
    }
}






