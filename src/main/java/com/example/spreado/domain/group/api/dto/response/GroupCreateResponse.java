package com.example.spreado.domain.group.api.dto.response;

public record GroupCreateResponse(
        Long groupId,
        String inviteLink
) {
}
