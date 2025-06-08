package com.team5.taketac.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// 카카오 길찾기 API의 전체 응답 구조
public class KakaoDirectionsResponse {
    @SerializedName("routes")
    private List<Route> routes; // 여러 경로가 있을 수 있으므로 리스트

    public List<Route> getRoutes() {
        return routes;
    }

    // 각 경로에 대한 상세 정보
    public static class Route {
        @SerializedName("summary")
        private Summary summary; // 경로 요약 정보 (거리, 시간 등)
        @SerializedName("sections")
        private List<Section> sections; // 경로의 각 구간

        public Summary getSummary() {
            return summary;
        }

        public List<Section> getSections() {
            return sections;
        }
    }

    // 경로 요약 정보
    public static class Summary {
        @SerializedName("distance")
        private int distance; // 총 거리 (미터)
        @SerializedName("duration")
        private int duration; // 총 시간 (초)
        // 기타 필드 (예: "fare", "waypoints" 등)는 필요에 따라 추가 가능
        public int getDistance() { return distance; }
        public int getDuration() { return duration; }
    }

    // 경로의 각 구간 (예: 도로, 보행자 도로)
    public static class Section {
        @SerializedName("roads")
        private List<Road> roads; // 구간을 구성하는 도로 정보

        public List<Road> getRoads() {
            return roads;
        }
    }

    // 도로 정보 (실제 경로의 좌표들을 포함)
    public static class Road {
        // vertexes는 [경도1, 위도1, 경도2, 위도2, ...] 형태로 연속된 좌표 리스트
        @SerializedName("vertexes")
        private List<Double> vertexes;

        public List<Double> getVertexes() {
            return vertexes;
        }
    }
}