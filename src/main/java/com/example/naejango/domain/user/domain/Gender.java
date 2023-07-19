package com.example.naejango.domain.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Gender {
    @JsonProperty("남")
    Male("남"),
    @JsonProperty("여")
    Female("여");

    private final String gender;

    public String getGender() {
        return gender;
    }

    Gender(String gender){
        this.gender = gender;
    }
}