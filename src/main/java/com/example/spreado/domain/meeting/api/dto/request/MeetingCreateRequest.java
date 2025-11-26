package com.example.spreado.domain.meeting.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MeetingCreateRequest(
        @NotNull(message = "그룹 ID는 필수입니다.")
        Long groupId,
        @NotBlank(message = "회의 이름은 필수입니다.")
        String title
) {
}
