package com.example.spreado.domain.meeting.core.entity;

import java.util.Arrays;

public enum MeetingStatus {
    ONGOING,
    ENDED;

    public static MeetingStatus from(String value) {
        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상태입니다: " + value));
    }
}
