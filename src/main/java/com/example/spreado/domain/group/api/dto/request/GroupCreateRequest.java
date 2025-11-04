package com.example.spreado.domain.group.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GroupCreateRequest(
        @NotBlank(message = "그룹 이름은 필수입니다.")
        String name,
        @NotBlank(message = "역할은 필수입니다.")
        String role
) {
}
