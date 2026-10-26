-- =====================================================================
-- 한자리(Hanjari) 개발용 샘플 데이터
-- 파일 위치: C:\hanjari\database\sample_data.sql
-- 실행 순서: schema.sql 실행 후 이 파일 실행
--
-- ⚠ 모든 식당은 검색 기능 테스트용 "가상" 데이터입니다. 실제 식당이 아닙니다.
--   실제 조사 데이터가 생기면 이 파일 대신 별도 파일로 등록합니다.
--   이미지 경로도 아직 실제 파일이 없는 자리표시용입니다.
-- =====================================================================

USE hanjari;

-- ---------------------------------------------------------------------
-- 식당 4곳 (id 1~4)
-- ---------------------------------------------------------------------
INSERT INTO restaurants (id, name, district, address, category, phone, business_hours, status, photo_consent_at, last_verified_at) VALUES
(1, '[샘플] 테스트 한식당 A', '마포구',   '서울특별시 마포구 샘플로 1',   '한식', NULL, '11:00-22:00', 'OPEN',   '2026-10-20', '2026-10-20'),
(2, '[샘플] 테스트 고깃집 B', '서대문구', '서울특별시 서대문구 샘플로 2', '고기', NULL, '16:00-24:00', 'OPEN',   '2026-10-20', '2026-10-20'),
(3, '[샘플] 테스트 중식당 C', '마포구',   '서울특별시 마포구 샘플로 3',   '중식', NULL, '11:30-21:30', 'OPEN',   '2026-10-20', '2026-10-20'),
(4, '[샘플] 폐업 테스트 D',   '마포구',   '서울특별시 마포구 샘플로 4',   '한식', NULL, NULL,          'CLOSED', NULL,         '2026-10-20');

-- ---------------------------------------------------------------------
-- 좌석 구성
-- ---------------------------------------------------------------------
INSERT INTO seating (restaurant_id, name, seating_type, min_capacity, max_capacity, quantity, description) VALUES
-- A: 4인 테이블 5개, 테이블 연결 시 최대 12명, 6~10인 룸 1개
(1, '4인 테이블',        'TABLE',     1,  4, 5, NULL),
(1, '연결 단체석',       'CONNECTED', 5, 12, 1, '4인 테이블 3개 연결'),
(1, '단체룸',            'ROOM',      6, 10, 1, '좌식'),
-- B: 4인 테이블 8개, 연결 시 최대 8명, 룸 없음
(2, '4인 테이블',        'TABLE',     1,  4, 8, NULL),
(2, '연결 단체석',       'CONNECTED', 5,  8, 1, '4인 테이블 2개 연결'),
-- C: 2~4인 테이블 6개, 8~16인 대형 룸 1개 (최소 8명부터 이용)
(3, '4인 테이블',        'TABLE',     2,  4, 6, NULL),
(3, '대형 룸',           'ROOM',      8, 16, 1, '원형 테이블 2개'),
-- D: 폐업 식당 (검색에 나오면 안 됨)
(4, '단체룸',            'ROOM',      4, 20, 1, NULL);

-- ---------------------------------------------------------------------
-- 사진 (경로만 기록, 실제 파일은 아직 없음)
-- ---------------------------------------------------------------------
INSERT INTO restaurant_images (restaurant_id, seating_id, image_url, image_type, caption, sort_order) VALUES
(1, NULL, '/images/sample/a-interior.jpg', 'INTERIOR',  '내부 전경',          0),
(1, NULL, '/images/sample/a-room.jpg',     'ROOM',      '단체룸 (6~10인)',    1),
(2, NULL, '/images/sample/b-connected.jpg','CONNECTED', '연결 단체석 (8인)',  0),
(3, NULL, '/images/sample/c-room.jpg',     'ROOM',      '대형 룸 (8~16인)',   0);

-- =====================================================================
-- 확인용 검색 쿼리 (나중에 Spring Boot에서 같은 조건으로 구현할 예정)
-- 아래 세 값을 바꿔 가며 실행해 보세요.
-- =====================================================================
SET @district = '마포구';   -- '마포구' / '서대문구' / NULL(전체)
SET @people   = 8;          -- 방문 인원
SET @room     = TRUE;       -- TRUE: 룸 필요 / FALSE: 상관없음

SELECT r.id, r.name, r.district,
       MAX(s.max_capacity) AS max_group_capacity
FROM restaurants r
JOIN seating s ON s.restaurant_id = r.id
WHERE r.status = 'OPEN'
  AND (@district IS NULL OR r.district = @district)
  AND s.min_capacity <= @people
  AND s.max_capacity >= @people
  AND (@room = FALSE OR s.seating_type = 'ROOM')
GROUP BY r.id, r.name, r.district
ORDER BY r.id;
