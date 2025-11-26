package com.example.spreado.domain.group.api.dto.response;

public record GroupJoinResponse(
        Long groupId,
        Long userId,
        String role
) {
}
