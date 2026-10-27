package com.hanjari.backend.restaurant;

// seating.seating_type 컬럼에 들어갈 수 있는 값
public enum SeatingType {
    TABLE,     // 고정 테이블
    CONNECTED, // 테이블을 붙여 만드는 단체석
    ROOM       // 독립된 룸
}
