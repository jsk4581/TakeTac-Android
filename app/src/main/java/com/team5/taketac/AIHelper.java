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
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);

        String prompt = "다음은 학생들의 채팅 내용과 시간표, 현재 시간입니다. " +
                "이를 참고하여 카풀 출발 시간을 정해줘.\n\n" +
                "⚠️ 출발 장소는 수진역, 복정역, 모란역, 태평역, 가천대역 중 하나여야 하고,\n" +
                "⚠️ 도착 장소는 글로벌캠퍼스, 공과대학1, 예술대학, 법과대학, 비전타워 중 하나여야 해.\n\n" +
                "다음 JSON 형식으로 결과를 줘:\n" +
                "{\"출발시간\": \"08:30\", \"출발장소\": \"복정역\", \"도착장소\": \"공과대학1\", \"판단근거\": \"학생 대부분이 9시 수업이며 복정역이 중간 위치임.\"}\n\n" +
                "채팅 내용:\n" + chatHistory + "\n\n" +
                "시간표:\n" + timetableJson + "\n\n" +
                "현재시간:\n" + mDate;

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("model", "openai/gpt-3.5-turbo");
            json.put("messages", new JSONArray()
                    .put(new JSONObject().put("role", "system").put("content", "너는 가천대학교 학생들을 위한 카풀 AI야. JSON으로만 응답해."))
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
