package com.example.naejango.domain.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Gender {
    @JsonProperty("남")
    Male,
    @JsonProperty("여")
    Female
}