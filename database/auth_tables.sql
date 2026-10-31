-- =====================================================================
-- 한자리(Hanjari) 로그인·단골 목록 테이블 추가
-- 파일 위치: C:\hanjari\database\auth_tables.sql
--
-- 이미 식당 데이터가 들어 있는 DB에 회원·단골 테이블만 추가할 때 실행합니다.
-- 기존 테이블과 데이터는 지우지 않습니다. (schema.sql에도 같은 테이블이 들어 있습니다)
--
-- 회원가입은 없습니다. 체험 계정은 서버가 켜질 때 .env의 DEMO_EMAIL, DEMO_PASSWORD로 만듭니다.
-- 비밀번호는 원문이 아니라 해시로만 저장합니다.
-- =====================================================================

USE hanjari;

-- ---------------------------------------------------------------------
-- users : 로그인 계정 (현재는 체험 계정 1개)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
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
CREATE TABLE IF NOT EXISTS favorites (
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
