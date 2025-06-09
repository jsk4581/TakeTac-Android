package com.team5.taketac;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AIHelper {
    public static void getRecommendedTime(String chatHistory, String timetableJson, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        long mNow;
        mNow =  System.currentTimeMillis();
        Date mDate;
        mDate = new Date(mNow);

        String prompt = "다음 채팅 내용과 시간표와 현재시간을 보고 가장 적절한 카풀 출발 시간을 추천해줘:\n\n" +
                "채팅:\n" + chatHistory + "\n\n" +
                "시간표:\n" + timetableJson + "\n\n" +
                "현재시간:\n" + mDate;;

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("model", "openai/gpt-3.5-turbo");
            json.put("messages", new JSONArray()
                    .put(new JSONObject().put("role", "system").put("content", "너는 카풀 출발 시간을 추천해주는 AI야. JSON 형식으로 결과를 줘. 예: {\"출발시간\": \"08:30\", \"출발장소\": \"강남역\"}"))
                    .put(new JSONObject().put("role", "user").put("content", prompt))
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer " + BuildConfig.OPENROUTER_API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}

