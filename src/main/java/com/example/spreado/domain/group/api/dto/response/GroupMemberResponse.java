package com.example.spreado.domain.group.api.dto.response;

public record GroupMemberResponse(
        Long userId,
        String name,
        String role
) {
}
