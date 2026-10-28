# 한자리 (Hanjari)

> 단체 인원일 때 어디 식당 갈까?

**한자리**는 단체 모임에 맞는 식당을 **실제 좌석 정보와 사진**으로 찾아 주는 웹서비스입니다.
"10명이 앉을 수 있는지", "룸이 있는지"를 가 보기 전에 확인할 수 있게 하는 것이 목표입니다.

대학생 4인 팀의 AI 경진대회 출품작입니다.

## 주요 기능

- **조건 검색**: 지역·인원·룸 여부로 식당 검색 (인원이 함께 앉을 수 있는 좌석이 있는 식당만)
- **식당 상세**: 좌석 구성(테이블·연결 단체석·룸, 인원 범위)과 좌석별 실제 사진
- **AI 자연어 검색**: "홍대에서 10명 회식할 건데 룸이면 좋겠어"처럼 문장으로 검색하면,
  Google Gemini가 조건을 이해하고 **DB에 있는 식당과 좌석 정보만 근거로** 추천 설명을 씁니다.
  AI는 식당을 지어내지 않고, 어떤 식당이 나올지는 항상 DB 검색이 정합니다.

## 데이터

- 마포구 식당 **8곳**을 팀원이 직접 방문해 조사했습니다 (2026-10-27).
- 모든 사진은 업주에게 **촬영·게시 동의**를 받았고, 얼굴과 차량 번호판은 모자이크, 위치 정보는 제거했습니다.
- 인터넷의 사진이나 정보를 가져다 쓰지 않습니다.

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 백엔드 | Java 21, Spring Boot 4.1.1, Spring Data JPA, Gradle |
| DB | MySQL 8 |
| 프론트엔드 | HTML, CSS, JavaScript (프레임워크 없음) |
| AI | Google Gemini API (`gemini-3.5-flash-lite`) |

## 폴더 구조

```
hanjari/
├─ backend/     Spring Boot 서버 (API, 식당 사진 제공)
├─ frontend/    화면 (HTML/CSS/JS)
├─ database/    schema.sql(테이블), real_data.sql(실제 데이터), sample_data.sql(테스트용)
└─ docs/        api-spec.md (API 명세)
```

## 실행 방법 (Windows)

### 1. DB 준비
MySQL Workbench에서 실행합니다.

1. 데이터베이스 만들기 (처음 한 번만)
   ```sql
   CREATE DATABASE hanjari CHARACTER SET utf8mb4;
   ```
2. `database/schema.sql` 실행 — 테이블 생성 (**기존 데이터는 지워집니다**)
3. `database/real_data.sql` 실행 — 실제 식당 8곳

`sample_data.sql`은 테스트용 가상 데이터입니다. `real_data.sql`과 id가 겹치므로 **둘 중 하나만** 넣습니다.

### 2. 환경변수 (`.env`)
프로젝트 맨 위 폴더에 `.env` 파일을 만듭니다. **이 파일은 Git에 올리지 않습니다.**

```
DB_PASSWORD=MySQL비밀번호
GEMINI_API_KEY=Gemini_API_키
```

- 줄 앞에 공백을 넣지 않습니다.
- 선택: `DB_USERNAME`(기본 `root`), `GEMINI_MODEL`(기본 `gemini-3.5-flash-lite`)
- Gemini 키는 [Google AI Studio](https://aistudio.google.com)에서 발급합니다. 키가 없어도 서버는 실행되며, AI 검색만 추천 설명 없이 일반 검색 결과로 응답합니다.

### 3. 백엔드 실행
VS Code에서 **F5** (`.vscode/launch.json`이 `.env`를 읽습니다).

확인: http://localhost:8080/api/restaurants

### 4. 프론트엔드 실행
VS Code **Live Server**로 `frontend/`를 엽니다 (http://127.0.0.1:5500).

## API

자세한 요청·응답 형식은 [docs/api-spec.md](docs/api-spec.md)를 따릅니다.

| 기능 | 요청 |
|---|---|
| 식당 목록·조건 검색 | `GET /api/restaurants?district=마포구&people=8&room=true` |
| 식당 상세 | `GET /api/restaurants/{id}` |
| AI 자연어 검색 | `POST /api/ai/search` `{ "query": "..." }` |
| 식당 사진 | `GET /images/restaurants/{id}/{파일}` |

## 일정

| 항목 | 날짜 |
|---|---|
| 중간보고서 | 2026년 11월 6일 |
| 최종 마감 | 2026년 12월 둘째 주 |

## 작업 규칙

- `main`에 직접 커밋하지 않고 `feature/...` 브랜치에서 작업한 뒤 PR로 합칩니다.
- API 응답 형식을 바꿀 때는 `docs/api-spec.md`를 먼저 고치고 팀에 알립니다.
- 비밀번호와 API 키는 `.env`에만 두고 코드·커밋에 넣지 않습니다.
