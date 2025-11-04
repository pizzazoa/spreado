package com.example.spreado.domain.group.api.dto.response;

import java.util.List;

public record GroupDetailResponse(
        Long groupId,
        String name,
        String inviteLink,
        String myRole,
        List<GroupMemberResponse> members
) {
}
