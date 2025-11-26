package com.example.spreado.domain.group.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GroupEmailInviteRequest(
        @NotNull(message = "이메일 목록은 필수입니다.")
        @NotEmpty(message = "최소 1개 이상의 이메일 주소가 필요합니다.")
        List<String> emails,

        String message
) {
}
