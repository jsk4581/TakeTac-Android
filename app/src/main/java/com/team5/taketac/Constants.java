package com.team5.taketac;

import com.kakao.vectormap.LatLng; // 카카오맵 SDK의 LatLng 클래스 임포트
import java.util.HashMap;
import java.util.Map;

public class Constants {

public static final String KAKAO_REST_API_KEY = BuildConfig.KAKAO_REST_API_KEY; // <-- ⭐⭐⭐ 이 부분을 실제 키로 변경하세요! ⭐⭐⭐

    // 카카오 길찾기 API Base URL
    public static final String KAKAO_DIRECTIONS_BASE_URL = "https://apis-navi.kakaomobility.com/";

    // 가천대학교 근처 지하철역 정보 (이름과 위도, 경도)
    public static final Map<String, LatLng> STATIONS = new HashMap<>();
    static {
        // 실제 역의 정확한 위도, 경도를 입력하세요.
        // 가천대역: 37.448550, 127.126588
        STATIONS.put("가천대역", LatLng.from(37.448550, 127.126588));
        // 복정역: 37.472714, 127.128795
        STATIONS.put("복정역", LatLng.from(37.472714, 127.128795));
        // 태평역: 37.441485, 127.139686
        STATIONS.put("태평역", LatLng.from(37.441485, 127.139686));
        // 모란역: 37.433895, 127.125712
        STATIONS.put("모란역", LatLng.from(37.433895, 127.125712));
        // 수진역: 37.438510, 127.142078
        STATIONS.put("수진역", LatLng.from(37.438510, 127.142078));
        // 기타 필요한 역들을 추가하세요...
    }

    // 가천대학교 학과 건물 정보 (이름과 위도, 경도)
    public static final Map<String, LatLng> BUILDINGS = new HashMap<>();
    static {
        // 실제 건물의 정확한 위도, 경도를 입력하세요.
        // 비전타워: 37.450123, 127.133210
        BUILDINGS.put("비전타워", LatLng.from(37.450123, 127.133210));
        // 공과대학1 (글로벌캠퍼스): 37.450890, 127.131970
        BUILDINGS.put("공과대학1", LatLng.from(37.450890, 127.131970));
        // 글로벌캠퍼스 (전체 영역의 중심 또는 대표 건물): 37.451700, 127.130950
        BUILDINGS.put("글로벌캠퍼스", LatLng.from(37.451700, 127.130950));
        // 예술대학: 37.452500, 127.132500
        BUILDINGS.put("예술대학", LatLng.from(37.452500, 127.132500));
        // 법과대학: 37.451000, 127.134000
        BUILDINGS.put("법과대학", LatLng.from(37.451000, 127.134000));
        // 기타 필요한 학과 건물들을 추가하세요...
    }
}