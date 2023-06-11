package com.example.naejango.domain.user.entity;

public enum Role {
    ADMIN, USER;
    @Override
    public String toString() {
        return "ROLE_"+super.toString();
    }
}
