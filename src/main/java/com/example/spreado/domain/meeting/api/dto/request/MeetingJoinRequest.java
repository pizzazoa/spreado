package com.example.spreado.domain.meeting.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record MeetingJoinRequest(
        @NotNull(message = "회의 ID는 필수입니다.")
        Long meetingId
) {
}
