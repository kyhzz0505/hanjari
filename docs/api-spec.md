# 한자리(Hanjari) API 명세서 v0.1

> 프론트엔드·백엔드 담당이 **이 문서를 기준으로 동시에 작업**합니다.
> 응답 형식을 바꿀 때는 반드시 이 문서를 먼저 수정하고 상대 담당자에게 알립니다.

## 기본 정보

| 항목 | 값 |
|---|---|
| 백엔드 주소 | `http://localhost:8080` |
| 프론트엔드 주소 | `http://127.0.0.1:5500` (VS Code Live Server) |
| 데이터 형식 | JSON (UTF-8) |
| CORS | 백엔드에서 `http://127.0.0.1:5500`, `http://localhost:5500` 허용 |

### 공통 값

| 필드 | 가능한 값 |
|---|---|
| `district` | `마포구` |
| `seatingType` | `TABLE`(일반 테이블), `CONNECTED`(연결 단체석), `ROOM`(룸) |
| `imageType` | `TABLE`, `CONNECTED`, `ROOM`, `INTERIOR`(내부 전경), `EXTERIOR`(외관·야외석) |

### 사진 주소
`mainImageUrl`, `imageUrl`은 `/images/restaurants/1/connected-1.jpg` 같은 경로입니다.
사진 파일은 백엔드가 제공하므로, 화면에서는 앞에 백엔드 주소를 붙입니다: `http://localhost:8080` + `imageUrl`

---

## 1. 식당 목록 · 조건 검색

```http
GET /api/restaurants
GET /api/restaurants?district=마포구&people=8&room=true
```

### 요청 파라미터 (모두 선택)

| 이름 | 타입 | 설명 | 생략 시 |
|---|---|---|---|
| `district` | string | 자치구 | 전체 지역 |
| `people` | number | 방문 인원 (1 이상) | 인원 조건 없음 |
| `room` | boolean | `true`면 해당 인원이 들어가는 룸이 있는 식당만 | `false` (상관없음) |

### 응답 `200 OK`

```json
[
  {
    "id": 1,
    "name": "[샘플] 테스트 한식당 A",
    "district": "마포구",
    "address": "서울특별시 마포구 샘플로 1",
    "category": "한식",
    "maxGroupCapacity": 12,
    "hasRoom": true,
    "mainImageUrl": "/images/sample/a-interior.jpg"
  }
]
```

| 필드 | 설명 |
|---|---|
| `maxGroupCapacity` | 이 식당에서 한 그룹이 함께 앉을 수 있는 최대 인원 |
| `hasRoom` | 룸 보유 여부 |
| `mainImageUrl` | 대표 사진 경로. 사진이 없으면 `null` |

- 결과가 없으면 빈 배열 `[]` 을 반환합니다.
- 영업 중(`OPEN`)인 식당만 반환합니다.

---

## 2. 식당 상세

```http
GET /api/restaurants/{id}
```

### 응답 `200 OK`

```json
{
  "id": 1,
  "name": "[샘플] 테스트 한식당 A",
  "district": "마포구",
  "address": "서울특별시 마포구 샘플로 1",
  "category": "한식",
  "phone": null,
  "businessHours": "11:00-22:00",
  "maxGroupCapacity": 12,
  "hasRoom": true,
  "lastVerifiedAt": "2026-10-20",
  "seatings": [
    { "id": 1, "name": "4인 테이블", "seatingType": "TABLE",     "minCapacity": 1, "maxCapacity": 4,  "quantity": 5, "description": null },
    { "id": 2, "name": "연결 단체석", "seatingType": "CONNECTED", "minCapacity": 5, "maxCapacity": 12, "quantity": 1, "description": "4인 테이블 3개 연결" },
    { "id": 3, "name": "단체룸",     "seatingType": "ROOM",      "minCapacity": 6, "maxCapacity": 10, "quantity": 1, "description": "좌식" }
  ],
  "images": [
    { "id": 1, "seatingId": null, "imageUrl": "/images/sample/a-interior.jpg", "imageType": "INTERIOR", "caption": "내부 전경",       "sortOrder": 0 },
    { "id": 2, "seatingId": null, "imageUrl": "/images/sample/a-room.jpg",     "imageType": "ROOM",     "caption": "단체룸 (6~10인)", "sortOrder": 1 }
  ]
}
```

### 오류 `404 Not Found`
없는 식당이거나 폐업·비공개 식당일 때

```json
{ "message": "식당을 찾을 수 없습니다." }
```

---

## 3. AI 자연어 검색 + 추천 설명

```http
POST /api/ai/search
Content-Type: application/json
```

### 요청

```json
{ "query": "마포구에서 10명 회식할 건데 룸이면 좋겠어" }
```

### 응답 `200 OK`

```json
{
  "conditions": { "district": "마포구", "people": 10, "room": true },
  "message": "10명이면 [샘플] 테스트 한식당 A의 단체룸(6~10인)이 인원에 딱 맞아요.",
  "restaurants": [ /* 1번 목록 API와 같은 형식 */ ]
}
```

- `conditions`: AI가 문장에서 뽑아낸 검색 조건. 화면에 "이렇게 이해했어요"로 보여 줍니다.
- `message`: AI가 **검색된 식당만 근거로** 작성한 추천 설명.
- AI 호출이 실패하면 `message`는 `null`, `restaurants`는 일반 검색 결과로 응답합니다. 이때 `conditions`는 문장의 "10명", "룸", "홍대" 같은 단어로 뽑은 값입니다.
- 검색 결과가 없으면 AI를 부르지 않고 안내 문장을 `message`로 보냅니다 (마포구 밖 지역이면 지역 안내 문장).
- 응답까지 보통 3~5초 걸립니다. 화면에 로딩 표시를 넣어 주세요.

### 오류 `400 Bad Request`
`query`가 비어 있을 때

```json
{ "message": "검색 문장을 입력해 주세요." }
```

`query`가 200자를 넘을 때

```json
{ "message": "검색 문장은 200자 이내로 입력해 주세요." }
```

---

## 프론트엔드 작업 팁
백엔드 API가 완성되기 전에는 위 응답 예시를 `frontend/mock/` 폴더에 JSON 파일로 저장해서 화면을 먼저 만듭니다.
API가 완성되면 `fetch` 주소만 `http://localhost:8080/api/...` 로 바꿉니다.

## 변경 이력
| 버전 | 날짜 | 내용 |
|---|---|---|
| v0.1 | 2026-10-26 | 최초 작성 (목록·검색, 상세, AI 검색) |
| v0.2 | 2026-10-28 | 지역을 마포구만으로 변경, `imageType`에 `EXTERIOR` 추가, 사진 주소 규칙 추가 |
| v0.3 | 2026-10-28 | AI 검색 구현: 결과 없음·지역 밖 안내 문장, 200자 제한, 응답 시간 안내 |
