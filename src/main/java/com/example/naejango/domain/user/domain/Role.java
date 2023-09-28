package com.example.naejango.domain.user.domain;

public enum Role {
    ADMIN,
    USER,
    GUEST,
    TEMPORAL, // UserProfile 을 작성하지 않은 회원
    DELETED, // 탈퇴 회원
    REJOINED // 탈퇴 후 재가입하여 다른 User 객체를 가지고 있는 회원
}
