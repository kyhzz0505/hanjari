package com.hanjari.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// users 테이블의 한 행: 로그인 계정 (지금은 체험 계정 1개뿐, 회원가입 없음)
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    // 비밀번호 원문이 아니라 PasswordHasher가 만든 해시만 저장한다
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String displayName;

    // created_at은 DB가 자동으로 채우므로 매핑하지 않는다.

    public User(String email, String passwordHash, String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    void update(String passwordHash, String displayName) {
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }
}
