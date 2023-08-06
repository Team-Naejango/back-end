package com.example.naejango.domain.user.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Gender {

    @JsonProperty("남")
    Male("남"),

    @JsonProperty("여")
    Female("여"),
    ;

    private final String gender;

    Gender(String gender) {this.gender = gender;}

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Gender get(String gender) {
        return Arrays.stream(values())
                .filter(type -> type.getGender().equals(gender))
                .findAny()
                .orElse(null);
    }

}