package com.team5.taketac.api;

import com.team5.taketac.model.OpenRouterRequest;
import com.team5.taketac.model.OpenRouterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenRouterService {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    Call<OpenRouterResponse> getChatCompletion(
            @retrofit2.http.Header("Authorization") String authHeader,
            @Body OpenRouterRequest request
    );
}
