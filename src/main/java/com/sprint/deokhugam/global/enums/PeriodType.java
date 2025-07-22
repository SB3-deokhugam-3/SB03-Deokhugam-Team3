package com.sprint.deokhugam.global.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PeriodType {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    ALL_TIME("ALL_TIME");

    private final String value;

    PeriodType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

}
