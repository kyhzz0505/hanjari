-- =====================================================================
-- 한자리(Hanjari) 데이터베이스 스키마 v0.1
-- 파일 위치: C:\hanjari\database\schema.sql
-- 실행 방법: MySQL Workbench에서 파일 열기 → 전체 실행(번개 아이콘)
--
-- ⚠ 주의: 아래 DROP TABLE 문은 기존 테이블과 데이터를 모두 지웁니다.
--         개발 초기(실제 데이터가 없을 때)에만 다시 실행하세요.
-- =====================================================================

USE hanjari;

-- DB 기본 문자셋을 utf8mb4로 맞춥니다 (한글·이모지 저장용)
ALTER DATABASE hanjari CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 참조 관계 역순으로 삭제 (단골 → 회원, 사진 → 좌석 → 식당)
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS restaurant_images;
DROP TABLE IF EXISTS seating;
DROP TABLE IF EXISTS restaurants;

-- ---------------------------------------------------------------------
-- 1) restaurants : 식당 기본 정보
-- ---------------------------------------------------------------------
CREATE TABLE restaurants (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    name              VARCHAR(100) NOT NULL,                 -- 식당명
    district          VARCHAR(20)  NOT NULL,                 -- 자치구: '마포구', '서대문구' (다른 구 확장 가능)
    address           VARCHAR(255) NOT NULL,                 -- 도로명 주소
    category          VARCHAR(30)  NOT NULL,                 -- 음식 종류: 한식, 중식, 고기 등
    phone             VARCHAR(20)  NULL,                     -- 매장 전화번호 (선택)
    business_hours    VARCHAR(255) NULL,                     -- 영업시간 (초기에는 자유 텍스트)
    status            VARCHAR(20)  NOT NULL DEFAULT 'OPEN',  -- OPEN(영업) / CLOSED(폐업) / HIDDEN(비공개)
    photo_consent_at  DATE         NULL,                     -- 업주 촬영·게시 동의 받은 날짜 (없으면 NULL)
    last_verified_at  DATE         NOT NULL,                 -- 좌석 정보 최종 확인일
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_restaurants_district_status (district, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 2) seating : 좌석 구성 (한 줄 = "한 그룹이 함께 앉을 수 있는 공간 한 종류")
--
--   seating_type
--     TABLE     : 고정 테이블 (4인 테이블, 8인 대형 테이블 등)
--     CONNECTED : 테이블을 붙여서 만드는 단체석 (예: 4인 테이블 3개 연결)
--     ROOM      : 독립된 룸
--
--   검색 기준: min_capacity <= 방문 인원 <= max_capacity 인 좌석이 하나라도 있으면 통과
-- ---------------------------------------------------------------------
CREATE TABLE seating (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    restaurant_id  BIGINT       NOT NULL,
    name           VARCHAR(50)  NOT NULL,                -- 표시 이름: '창가 4인 테이블', '단체룸 A'
    seating_type   VARCHAR(20)  NOT NULL,                -- TABLE / CONNECTED / ROOM
    min_capacity   INT          NOT NULL DEFAULT 1,      -- 최소 이용 인원 (예: 룸 6인 이상)
    max_capacity   INT          NOT NULL,                -- 한 그룹이 함께 앉을 수 있는 최대 인원
    quantity       INT          NOT NULL DEFAULT 1,      -- 같은 구성의 개수 (예: 4인 테이블 5개)
    description    VARCHAR(255) NULL,                    -- 보충 설명: '4인 테이블 3개 연결', '좌식'
    PRIMARY KEY (id),
    CONSTRAINT fk_seating_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE,
    CONSTRAINT chk_seating_capacity
        CHECK (min_capacity >= 1 AND min_capacity <= max_capacity),
    CONSTRAINT chk_seating_quantity
        CHECK (quantity >= 1),
    INDEX idx_seating_search (restaurant_id, seating_type, max_capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 3) restaurant_images : 좌석 사진
--   seating_id가 있으면 특정 좌석 사진, NULL이면 식당 전체(내부 전경) 사진
--   sort_order가 가장 작은 사진을 대표 사진으로 사용
-- ---------------------------------------------------------------------
CREATE TABLE restaurant_images (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    restaurant_id  BIGINT       NOT NULL,
    seating_id     BIGINT       NULL,
    image_url      VARCHAR(500) NOT NULL,                -- 예: /images/restaurants/1/room-a.jpg
    image_type     VARCHAR(20)  NOT NULL,                -- TABLE / CONNECTED / ROOM / INTERIOR / EXTERIOR
    caption        VARCHAR(100) NULL,                    -- 사진 설명
    sort_order     INT          NOT NULL DEFAULT 0,
    uploaded_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_images_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE,
    CONSTRAINT fk_images_seating
        FOREIGN KEY (seating_id) REFERENCES seating (id) ON DELETE SET NULL,
    INDEX idx_images_restaurant (restaurant_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- ---------------------------------------------------------------------
-- users : 로그인 계정 (현재는 체험 계정 1개)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    email          VARCHAR(254) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,                -- pbkdf2$반복횟수$솔트$해시
    display_name   VARCHAR(50)  NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- favorites : 회원별 단골 식당
-- ---------------------------------------------------------------------
CREATE TABLE favorites (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    restaurant_id  BIGINT       NOT NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_favorites_user_restaurant (user_id, restaurant_id),
    CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
