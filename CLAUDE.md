# 한자리(Hanjari) — Claude Code 작업 안내

단체 모임용 식당 좌석 정보 검색 웹서비스. 대학생 4인 팀, AI 경진대회 출품작.
지역(마포구·서대문구)·인원·룸 조건으로 식당을 찾고 실제 좌석 사진을 보여 준다.

## 기술 스택 (변경 금지)
- 백엔드: Java 21, Spring Boot 4.1.1, Gradle(Groovy), Spring Data JPA, Lombok — `backend/`
- DB: MySQL 8, 스키마는 `database/schema.sql`이 기준 (JPA `ddl-auto: validate`)
- 프론트: 순수 HTML/CSS/JavaScript (React 사용 안 함) — `frontend/`, Live Server(127.0.0.1:5500)
- 서비스 AI: Google Gemini API (백엔드에서만 호출)
- 패키지: `com.hanjari.backend`, 기능별 패키지 (예: `com.hanjari.backend.restaurant`)

## 기준 문서
- API 명세: `docs/api-spec.md` — 응답 필드 이름·형식은 이 문서를 따른다. 바꿔야 하면 먼저 문서를 수정하고 이유를 말한다.
- DB: `database/schema.sql`, 샘플 데이터 `database/sample_data.sql` (모두 `[샘플]` 가상 식당)

## 검색 규칙
status = 'OPEN' AND (district 일치, 생략 시 전체) AND
min_capacity <= people <= max_capacity 인 seating이 하나라도 존재 AND
(room=true면 그 seating의 seating_type = 'ROOM')

## 작업 원칙
- 개발자는 Spring Boot 초보. 한 번에 한 단계씩, 파일 경로를 정확히 말하고 왜 그렇게 하는지 짧게 설명한다.
- 기존 구조·설정을 임의로 바꾸지 않는다. 의존성 추가가 필요하면 이유와 함께 먼저 묻는다.
- 실제로 실행·확인되지 않은 것을 완료됐다고 말하지 않는다.
- 비밀값(DB 비밀번호, GEMINI_API_KEY)은 `.env`/환경변수로만 다루고 코드·커밋에 넣지 않는다.
- 존재하지 않는 식당 데이터를 실제처럼 만들지 않는다. AI는 DB 검색 결과만 근거로 추천한다.
- 제외 기능: 예약·결제·리뷰·평점·날짜별 빈자리·회원가입·지하철역 검색.
- 담당: 백엔드 개발자는 `backend/`, `database/`만 수정한다. `frontend/`는 프론트 담당 영역.
- `main`에 직접 커밋하지 않는다. `feature/...` 브랜치에서 작업한다.

## 실행 (Windows PowerShell)
- 서버: VS Code F5 (`.vscode/launch.json`이 루트 `.env`를 읽음)
  또는 `cd backend; $env:DB_PASSWORD="..."; .\gradlew.bat bootRun`
- 확인: http://localhost:8080/api/restaurants
