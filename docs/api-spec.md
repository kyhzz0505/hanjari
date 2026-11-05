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
| `maxGroupCapacity` | 이 식당에서 한 그룹이 함께 앉을 수 있는 최대 인원. 좌석 정보가 없으면 `null` ("좌석 정보 없음"으로 표시) |
| `hasRoom` | 룸 보유 여부 |
| `mainImageUrl` | 대표 사진 경로. 사진이 없으면 `null` |

- 결과가 없으면 빈 배열 `[]` 을 반환합니다.
- 영업 중(`OPEN`)인 식당만 반환합니다.

### 오류 `400 Bad Request`
`people`가 1보다 작을 때 (0명, 음수)

```json
{ "message": "인원은 1명 이상이어야 합니다." }
```

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

### 오류 `429 Too Many Requests`
같은 접속지에서 1분에 10번을 넘게 요청했을 때 (Gemini 사용량 보호). 응답 헤더 `Retry-After`에 다시 시도할 수 있을 때까지 남은 초가 들어갑니다.

```json
{ "message": "AI 검색 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요." }
```

---

## 4. 로그인 (체험 계정)

회원가입은 없습니다. **팀만 아는 체험 계정 1개**로만 로그인할 수 있습니다.
체험 계정의 이메일·비밀번호는 각자 `.env`의 `DEMO_EMAIL`, `DEMO_PASSWORD`에 넣으며, 코드·커밋에는 넣지 않습니다.
로그인 상태는 서버 세션 쿠키로 유지됩니다.

### 프론트 연결 시 주의
- 로그인 이후 모든 요청에 `fetch(url, { credentials: 'include' })`를 붙여야 쿠키가 함께 갑니다.
- Live Server로 개발할 때는 화면 주소와 API 주소의 호스트를 맞춥니다: `127.0.0.1:5500` → `http://127.0.0.1:8080`
  (`127.0.0.1`과 `localhost`는 서로 다른 사이트로 취급돼 쿠키가 전달되지 않습니다)

### `POST /api/auth/login`
```json
{ "email": "체험 계정 이메일", "password": "체험 계정 비밀번호" }
```
- `200 OK`: `{ "email": "...", "displayName": "체험 회원" }`
- `401 Unauthorized`: `{ "message": "이메일 또는 비밀번호가 올바르지 않습니다." }`
- `400 Bad Request`: 이메일·비밀번호가 비어 있을 때 `{ "message": "이메일과 비밀번호를 입력해 주세요." }`
- `429 Too Many Requests`: 같은 접속지에서 10분 안에 5번 틀렸을 때, 남은 시간 동안은 맞는 비밀번호도 막습니다. 로그인에 성공하면 틀린 횟수는 지워집니다. 응답 헤더 `Retry-After`에 남은 초가 들어갑니다.
  `{ "message": "로그인 시도가 너무 많습니다. 7분 뒤에 다시 시도해 주세요." }` (분은 남은 시간에 따라 바뀜)

### `GET /api/auth/me`
현재 로그인 상태 확인
- `200 OK`: `{ "email": "...", "displayName": "체험 회원" }`
- `401 Unauthorized`: `{ "message": "로그인이 필요합니다." }`

### `POST /api/auth/logout`
- `204 No Content`

---

## 5. 단골 목록 (로그인 필요)

로그인하지 않으면 모두 `401 { "message": "로그인이 필요합니다." }`

### `GET /api/favorites`
단골로 저장한 식당 목록. 최근에 추가한 순서. 형식은 1번 목록 API와 같습니다.
영업 중이 아닌 식당은 빠집니다.

### `PUT /api/favorites/{restaurantId}`
단골에 추가. 이미 추가된 식당이면 그대로 둡니다.
- `204 No Content`
- `404 Not Found`: 없는 식당이거나 영업 중이 아닌 식당 `{ "message": "식당을 찾을 수 없습니다." }`

### `DELETE /api/favorites/{restaurantId}`
단골에서 삭제. 단골이 아니어도 그대로 성공합니다.
- `204 No Content`

체험 계정은 여러 사람이 함께 쓰므로, 한 사람이 추가한 단골이 다른 사람에게도 보입니다.

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
| v0.4 | 2026-10-28 | 목록·검색: `people`가 1 미만이면 400 |
| v0.5 | 2026-10-28 | `maxGroupCapacity`: 좌석 정보가 없으면 `null` 명시 |
| v0.6 | 2026-10-31 | 체험 계정 로그인(4번), 단골 목록(5번) 추가 |
| v0.7 | 2026-11-05 | 횟수 제한: 로그인 10분에 5번 실패, AI 검색 1분에 10번을 넘으면 `429` |
