package com.programmers.kdt.user.entity;

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
    private String password;

    @Enumerated(EnumType.STRING)
    private Role userType;

    private String businessName;
    private String businessNumber;
    private String settlementAccount;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private User(String username, String email, String encodedPassword, Role userType) {
        this.username = username;
        this.email = email;
        this.password = encodedPassword;
        this.userType = userType;
        this.status = UserStatus.ACTIVE;
    }

    private User(String username, String email, String encodedPassword, Role userType,
                  String businessName, String businessNumber) {
        this(username, email, encodedPassword, userType);
        this.businessName = businessName;
        this.businessNumber = businessNumber;
    }

    public static User signUpIndividual(String username, String email, String encodedPassword) {
        return new User(username, email, encodedPassword, Role.INDIVIDUAL);
    }

    public static User signUpBusiness(String username, String email, String encodedPassword,
                                       String businessName, String businessNumber) {
        return new User(username, email, encodedPassword, Role.BUSINESS, businessName, businessNumber);
    }

    public boolean isWithdrawn() {
        return this.status == UserStatus.WITHDRAWN;
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
    }
}
