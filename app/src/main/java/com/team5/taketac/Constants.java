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
        
        STATIONS.put("가천대역", LatLng.from(37.44960200000001, 127.126635));
        STATIONS.put("복정역", LatLng.from(37.470375, 127.126685));
        STATIONS.put("태평역", LatLng.from(37.439854, 127.128035));
        STATIONS.put("모란역", LatLng.from(37.432124, 127.129064));
        STATIONS.put("모란역", LatLng.from(37.432124, 127.129064));
        STATIONS.put("test", LatLng.from(37.4551254, 127.1334847));
    }

    // 가천대학교 학과 건물 정보 (이름과 위도, 경도)
    public static final Map<String, LatLng> BUILDINGS = new HashMap<>();
    static {
        BUILDINGS.put("비전타워", LatLng.from(37.4494941, 127.1271216));
        BUILDINGS.put("산악협력관1", LatLng.from(37.4507128, 127.1288495));
        BUILDINGS.put("예술체육대학1", LatLng.from(37.4507128, 127.1288495));
        BUILDINGS.put("교육대학원", LatLng.from(37.4519255, 127.1318083));
        BUILDINGS.put("학생회관", LatLng.from(37.4503017, 127.1301802));
        BUILDINGS.put("AI관", LatLng.from(37.4507128, 127.1288495));
    }
}