package com.team5.taketac.api;

import com.team5.taketac.model.KakaoDirectionsResponse; // 다음 단계에서 만들 모델 클래스 임포트
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface KakaoDirectionsService {
    // 카카오 길찾기 API 호출 정의
    @GET("v1/directions") // API 엔드포인트
    Call<KakaoDirectionsResponse> getDirections(
            @Header("Authorization") String authorization, // REST API 키를 포함한 인증 헤더
            @Query("origin") String origin,          // 출발지 좌표 (경도,위도 형식)
            @Query("destination") String destination // 도착지 좌표 (경도,위도 형식)
            // 필요한 경우, "waypoints", "priority", "alternatives" 등의 쿼리 파라미터를 추가할 수 있습니다.
            // 여기서는 기본 길찾기만 사용합니다.
    );
}