package com.example.naejango.domain.item.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ItemType {
    INDIVIDUAL_BUY, INDIVIDUAL_SELL, GROUP_BUY;

    @JsonCreator
    public static ItemType from(String value) {
        for (ItemType itemType : ItemType.values()) {
            if (itemType.name().equals(value)) {
                return itemType;
            }
        }
        return null;
    }
}
