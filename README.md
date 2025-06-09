- # TAKE-TAC: 가천대학교 택시 카풀 매칭 앱

- ## 개발환경
- Android Studio
- Java 21
- Firebase

## 목차

1. [프로젝트 개요](#프로젝트-개요)  
2. [기능 요약](#기능-요약)  
3. [전체 구조](#전체-구조)  
4. [화면 구성 및 주요 UI](#화면-구성-및-주요-ui)  
5. [주요 클래스 설명](#주요-클래스-설명)  
6. [Firebase 연동](#firebase-연동)  
7. [외부 API 연동](#외부-api-연동)  
8. [추가 정보](#추가-정보)

---

## 프로젝트 개요

**TAKE-TAC**은 가천대학교 학생들의 택시 카풀을 돕는 Android 기반 앱입니다. 

학생들은 시간표와 위치 정보를 기반으로 자동/공개 매칭되어 카풀 상대를 찾고, 도착지에 도달하면 카카오택시 앱으로 전환할 수 있습니다.

---

## 기능 요약

- 출발지/도착지 선택 및 매칭 요청
- 인원 수 기반 매칭 대기/완료 처리
- 카카오맵을 활용한 실시간 위치 공유 및 경로 안내
- 채팅방 생성 및 추천 출발 시간 AI 기능
- 공개 파티 생성/참여/수정/삭제 기능
- 시간표 등록 및 시각화
- 회원가입, 로그인, 프로필 편집(닉네임, 주소, 비밀번호, 프로필 사진)

---

## 전체 구조

### 액티비티/프래그먼트 구성

- `LoginActivity`, `SignupActivity`: 인증 화면
- `MainActivity`: `BottomNavigationView`로 Fragment 전환
- `HomeFragment`: 카풀 매칭 요청 및 매칭 대기
- `MapFragment`: 실시간 위치/경로 확인
- `ProfileFragment`: 사용자 정보 수정
- `TimetableFragment`: 시간표 관리
- `PublicMatchingActivity`, `PartyFragment`: 공개 파티 리스트
- `CreatePublicPartyActivity`: 공개 파티 생성
- `ChatActivity`: 실시간 채팅
- `PartyDetailBottomSheetFragment`, `EditPartyDialogFragment`: 공개 파티 상세/수정 UI

---

## 화면 구성 및 주요 UI

### `fragment_home.xml`

- 출발지/도착지 선택용 Spinner
- 매칭 버튼 / 매칭 대기 상태 전환
- 매칭 인원 수, 지도 보기, 매칭 취소 버튼 표시
- 하단에 채팅방 목록 표시

### `fragment_map.xml`

- 카카오맵(`MapView`)과 경로 표시 (`RouteLineManager`)
- 참여자 위치를 마커로 표현하고 실시간 갱신
- 홈 화면 복귀 버튼 포함

---

## 주요 클래스 설명

| 클래스명 | 역할 |
|----------|------|
| `LoginActivity` | FirebaseAuth를 활용한 이메일 로그인 처리 |
| `SignupActivity` | 회원가입 및 주소 검색 연동 |
| `MainActivity` | Fragment 전환 및 로그인 상태 검사 |
| `HomeFragment` | 카풀 매칭 요청, UI 전환, GPS 기반 도착 처리 |
| `MapFragment` | Kakao Map + Kakao Directions API 연동 |
| `ProfileFragment` | FirebaseStorage와 연동한 프로필 수정 |
| `ChatActivity` | Firebase Realtime Database를 활용한 실시간 채팅 |
| `CreatePublicPartyActivity` | Firestore + RealtimeDB에 공개 파티 등록 |
| `PartyDetailBottomSheetFragment` | 파티 상세 정보 및 참여/수정/삭제 동작 제공 |
| `AIHelper` | OpenRouter 기반 AI 출발시간 추천 요청 처리 |

---

## Firebase 연동

- **Authentication**: 이메일/비밀번호 기반 로그인
- **Realtime Database**:
  - `/users/{uid}`: 닉네임, 주소, 위치 정보 저장
  - `/partyRooms/{partyId}`: 파티 정보 및 참여자 목록
  - `/chatRooms/{chatRoomId}`: 채팅 메시지 저장
- **Firestore**:
  - `publicParties`: 공개 파티 리스트 조회 및 관리
  - `users/{uid}/timetable`: 시간표 저장 (요일, 시작/종료 시간, 강의명)

---

## 외부 API 연동

### Kakao Map SDK (Vector Map v2)
- `MapView` 위젯을 기반으로 실시간 위치/경로 시각화
- `Label`, `RouteLine`, `CameraUpdate` 활용
- 참여자 위치 마커 및 도착 검증 기능 포함

### Kakao Directions API
- 출발지~도착지 간 경로를 Polyline으로 표시
- `KakaoDirectionsService` + `KakaoDirectionsResponse` 모델 활용

### OpenRouter (AI 출발시간 추천)
- `AIHelper`에서 사용자 채팅 및 시간표 데이터를 기반으로 출발 시간 추천 요청
- JSON 포맷 응답 (예: `{ "출발시간": "08:30" }`)

---

## 추가 정보

### Constants 정의

- `Constants.java`에는 Kakao API 키, 지하철역/학과 건물 좌표 정보가 하드코딩
- `LatLng` 객체를 활용하여 지도 기반 UI 구현 지원

