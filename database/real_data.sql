-- =====================================================================
-- 한자리(Hanjari) 실제 식당 데이터 (2026-10-27 현장 조사)
-- 파일 위치: C:\hanjari\database\real_data.sql
-- 실행 순서: schema.sql 실행 후 이 파일 실행 (sample_data.sql 대신 사용)
--            두 파일 모두 id 1번부터 쓰므로 함께 넣으면 충돌합니다.
--
-- 모든 식당은 업주에게 촬영·게시 동의를 받았습니다 (photo_consent_at).
-- 일반 4인석처럼 작은 좌석은 넣지 않고, 단체로 쓸 수 있는 좌석만 넣습니다.
-- 사진 파일: backend/src/main/resources/static/images/restaurants/{식당 id}/
-- =====================================================================

USE hanjari;

-- ---------------------------------------------------------------------
-- 식당 8곳 (모두 마포구)
-- ---------------------------------------------------------------------
INSERT INTO restaurants (id, name, district, address, category, phone, business_hours, status, photo_consent_at, last_verified_at) VALUES
(1, '공감 합정점',            '마포구', '서울특별시 마포구 양화로6길 49 2층',     '이자카야', '02-333-4733',   '16:00-02:00 (라스트오더 01:00)',                                                  'OPEN', '2026-10-27', '2026-10-27'),
(2, '대나무 숙성회관 홍대점', '마포구', '서울특별시 마포구 동교로 212-9 1층',     '회',       '0507-1495-9049', '14:00-00:30 (라스트오더 23:30)',                                                  'OPEN', '2026-10-27', '2026-10-27'),
(3, '도적 합정점',            '마포구', '서울특별시 마포구 어울마당로5길 3',      '고기',     '02-322-6746',   '16:00-00:30',                                                                     'OPEN', '2026-10-27', '2026-10-27'),
(4, '로얄호프치킨 합정점',    '마포구', '서울특별시 마포구 양화로6길 53 1층',     '치킨',     '0507-1318-4465', '월·화·수·목·일 15:00-03:00, 금·토 15:00-05:00',                                   'OPEN', '2026-10-27', '2026-10-27'),
(5, '이대포 사골부대찌개',    '마포구', '서울특별시 마포구 와우산로29라길 12',    '부대찌개', '02-322-6502',   '11:00-20:00 (평일 브레이크타임 15:00-17:00, 라스트오더 평일 19:20·주말 19:50)', 'OPEN', '2026-10-27', '2026-10-27'),
(6, '전통솥뚜껑삼겹살',       '마포구', '서울특별시 마포구 홍익로5안길 16 1층',   '고기',     NULL,            '12:00-24:00 (라스트오더 23:30)',                                                  'OPEN', '2026-10-27', '2026-10-27'),
(7, '중화복춘골드',           '마포구', '서울특별시 마포구 월드컵북로6길 42 101호', '중식',   '0507-1435-2209', '11:30-22:00 (브레이크타임 15:30-17:00, 라스트오더 14:40·21:10)',               'OPEN', '2026-10-27', '2026-10-27'),
(8, '청기와타운 홍대점',      '마포구', '서울특별시 마포구 양화로 128 1층',       '고기',     '02-3144-0462',  '11:15-22:00 (라스트오더 21:00)',                                                  'OPEN', '2026-10-27', '2026-10-27');

-- ---------------------------------------------------------------------
-- 좌석 구성 (사진과 연결하기 위해 id를 직접 지정)
-- ---------------------------------------------------------------------
INSERT INTO seating (id, restaurant_id, name, seating_type, min_capacity, max_capacity, quantity, description) VALUES
-- 공감 합정점: 큰 룸 1개 + 4인 룸 2개를 칸막이로 이은 연결 룸 2쌍 (4인 룸 단독은 넣지 않음)
( 1, 1, '단체룸',      'ROOM',      2, 11, 1, '좌식'),
( 2, 1, '연결 룸',     'ROOM',      6,  8, 2, '4인 룸 2개, 나무 칸막이를 열어 사용'),
-- 대나무 숙성회관: 오픈형 룸 2개
( 3, 2, '단체룸',      'ROOM',      5,  6, 2, '좌식, 오픈형 룸'),
-- 도적: 6인 이상 단체는 2층으로 안내 (최대 40명, 통대관)
( 4, 3, '2층 단체석',  'CONNECTED', 6, 40, 1, '2층 통대관, 최대 40명'),
-- 로얄호프치킨: 4인 테이블 3개 연결, 6인 테이블
( 5, 4, '연결 단체석', 'CONNECTED', 5, 12, 1, '4인 테이블 3개 연결'),
( 6, 4, '6인 테이블',  'TABLE',     4,  6, 1, NULL),
-- 이대포: 4인 테이블(총 8개) 2개 연결
( 7, 5, '연결 단체석', 'CONNECTED', 6,  8, 1, '4인 테이블 2개 연결'),
-- 전통솥뚜껑삼겹살: 테이블을 붙여 최대 16명
( 8, 6, '연결 단체석', 'CONNECTED', 6, 16, 1, '4인 테이블 4개 + 2인 테이블 2개 연결'),
-- 중화복춘골드: 커튼으로 가릴 수 있는 원형 단체석 2개, 두 테이블을 합치면 최대 8명
( 9, 7, '단체석',      'TABLE',     4,  6, 2, '원형 테이블, 커튼으로 가릴 수 있음'),
-- 청기와타운: 단체룸, 6인 테이블 2개(연결 시 12명), 8인 테이블
(10, 8, '단체룸',      'ROOM',      5,  6, 1, '좌식'),
(11, 8, '6인 테이블',  'TABLE',     5,  6, 2, NULL),
(12, 8, '연결 단체석', 'CONNECTED', 6, 12, 1, '6인 테이블 2개 연결'),
(13, 8, '8인 테이블',  'TABLE',     6,  8, 1, NULL),
-- 중화복춘골드 추가 (기존 id를 바꾸지 않도록 마지막에 둠)
(14, 7, '연결 단체석', 'CONNECTED', 6,  8, 1, '단체석 2개 연결, 커튼으로 가릴 수 있음');

-- ---------------------------------------------------------------------
-- 사진 (sort_order가 가장 작은 사진이 대표 사진)
-- 얼굴·차량 번호판은 모자이크 처리, 위치 정보(EXIF)는 제거한 파일
-- ---------------------------------------------------------------------
INSERT INTO restaurant_images (restaurant_id, seating_id, image_url, image_type, caption, sort_order) VALUES
(1,    1, '/images/restaurants/1/room-1.jpg',      'ROOM',      '단체룸 (최대 11인)',          0),
(1,    2, '/images/restaurants/1/room-2.jpg',      'ROOM',      '연결 룸 (6~8인)',             1),
(1,    2, '/images/restaurants/1/room-3.jpg',      'ROOM',      '4인 룸 (칸막이를 열면 8인)',  2),
(1, NULL, '/images/restaurants/1/interior-1.jpg',  'INTERIOR',  '내부 전경',                   3),
(1, NULL, '/images/restaurants/1/interior-2.jpg',  'INTERIOR',  '홀 좌석',                     4),
(1, NULL, '/images/restaurants/1/interior-3.jpg',  'INTERIOR',  '창가 좌석',                   5),
(1, NULL, '/images/restaurants/1/exterior-1.jpg',  'EXTERIOR',  '입구 (2층)',                  6),

(2,    3, '/images/restaurants/2/room-1.jpg',      'ROOM',      '오픈형 룸 (5~6인)',           0),
(2, NULL, '/images/restaurants/2/interior-1.jpg',  'INTERIOR',  '내부 전경',                   1),
(2, NULL, '/images/restaurants/2/interior-2.jpg',  'INTERIOR',  '칸막이 좌석',                 2),
(2, NULL, '/images/restaurants/2/exterior-1.jpg',  'EXTERIOR',  '외관',                        3),

(3,    4, '/images/restaurants/3/connected-1.jpg', 'CONNECTED', '단체석',                      0),
(3, NULL, '/images/restaurants/3/interior-1.jpg',  'INTERIOR',  '내부 전경',                   1),
(3, NULL, '/images/restaurants/3/interior-2.jpg',  'INTERIOR',  '홀 좌석',                     2),
(3, NULL, '/images/restaurants/3/exterior-1.jpg',  'EXTERIOR',  '외관',                        3),

(4,    5, '/images/restaurants/4/connected-1.jpg', 'CONNECTED', '4인 테이블 3개 연결 (최대 12인)', 0),
(4,    6, '/images/restaurants/4/table-1.jpg',     'TABLE',     '6인 테이블',                  1),
(4, NULL, '/images/restaurants/4/interior-1.jpg',  'INTERIOR',  '내부 전경',                   2),
(4, NULL, '/images/restaurants/4/exterior-1.jpg',  'EXTERIOR',  '외관',                        3),
(4, NULL, '/images/restaurants/4/exterior-2.jpg',  'EXTERIOR',  '야외 테라스석',               4),

(5,    7, '/images/restaurants/5/connected-1.jpg', 'CONNECTED', '4인 테이블 2개 연결 (6~8인)', 0),
(5, NULL, '/images/restaurants/5/interior-1.jpg',  'INTERIOR',  '내부 전경',                   1),
(5, NULL, '/images/restaurants/5/exterior-1.jpg',  'EXTERIOR',  '외관',                        2),

(6,    8, '/images/restaurants/6/connected-1.jpg', 'CONNECTED', '연결 단체석 (최대 16인)',     0),
(6,    8, '/images/restaurants/6/connected-2.jpg', 'CONNECTED', '연결 단체석',                 1),
(6,    8, '/images/restaurants/6/connected-3.jpg', 'CONNECTED', '연결 단체석',                 2),
(6, NULL, '/images/restaurants/6/exterior-1.jpg',  'EXTERIOR',  '외관',                        3),

(7,    9, '/images/restaurants/7/table-1.jpg',     'TABLE',     '단체석 (커튼으로 가림 가능)', 0),
(7, NULL, '/images/restaurants/7/exterior-1.jpg',  'EXTERIOR',  '입구',                        1),

(8,   10, '/images/restaurants/8/room-1.jpg',      'ROOM',      '단체룸 (최대 6인)',           0),
(8,   12, '/images/restaurants/8/connected-1.jpg', 'CONNECTED', '6인 테이블 2개 (최대 12인)',  1),
(8,   13, '/images/restaurants/8/table-1.jpg',     'TABLE',     '8인 테이블',                  2),
(8, NULL, '/images/restaurants/8/interior-1.jpg',  'INTERIOR',  '내부 전경',                   3);
