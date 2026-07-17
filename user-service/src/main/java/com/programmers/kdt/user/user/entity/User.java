package com.programmers.kdt.user.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 (ERD: User) — 팀 요청으로 Member에서 User로 변경
 * 주의: MySQL 예약어 회피 위해 테이블명은 app_user로 지정
 */
@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role userType;

    private String businessName;
    private String businessNumber;
    private String settlementAccount;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // TODO: 정적 팩토리 메서드(signUp), 비즈니스 메서드(withdraw)
}
