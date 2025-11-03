package com.example.spreado.domain.group.core.entity;

import java.util.Arrays;

public enum GroupRole {
    PM,
    BE,
    FE,
    PD,
    AI;

    public static GroupRole from(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 역할입니다: " + value));
    }
}
