package com.example.spreado.domain.summary.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SummaryUpdateRequest(
        @NotBlank(message = "summaryJson은 필수입니다.")
        String summaryJson
) {
}
