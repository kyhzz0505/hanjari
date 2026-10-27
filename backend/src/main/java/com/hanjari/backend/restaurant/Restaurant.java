package com.hanjari.backend.restaurant;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// restaurants 테이블의 한 행(식당 하나)을 나타내는 JPA 엔티티
// 테이블 구조는 database/schema.sql이 기준이고, 여기서는 그 구조를 그대로 옮겨 적는다.
@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA가 객체를 만들 때 쓰는 기본 생성자
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String district;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(length = 20)
    private String phone;

    private String businessHours;

    // DB에는 'OPEN' 같은 문자열(VARCHAR)로 저장된다.
    // JdbcTypeCode가 없으면 Hibernate가 MySQL ENUM 타입을 기대해서 validate에서 실패한다.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RestaurantStatus status;

    private LocalDate photoConsentAt;

    @Column(nullable = false)
    private LocalDate lastVerifiedAt;

    // created_at, updated_at은 DB가 자동으로 채우므로 아직 매핑하지 않는다.
}
