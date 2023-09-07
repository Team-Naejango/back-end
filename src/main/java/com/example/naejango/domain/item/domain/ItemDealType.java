package com.example.naejango.domain.item.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ItemDealType {
    INDIVIDUAL, GROUP;

    @JsonCreator
    public static ItemDealType from(String value) {
        for (ItemDealType itemDealType : ItemDealType.values()) {
            if (itemDealType.name().equals(value)) {
                return itemDealType;
            }
        }
        return null;
    }
}
