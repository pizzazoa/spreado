package com.example.spreado.domain.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateNameRequest(
        @NotBlank(message = "변경할 이름을 입력해주세요.")
        String name
) {
}
