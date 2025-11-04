package com.example.spreado.domain.group.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GroupJoinRequest(
        @NotBlank(message = "초대 링크는 필수입니다.")
        String inviteLink,
        @NotBlank(message = "역할은 필수입니다.")
        String role
) {
}
