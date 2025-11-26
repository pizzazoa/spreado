package com.example.spreado.domain.group.api.dto.response;

public record GroupSummaryResponse(
        Long groupId,
        String name,
        String role
) {
}
