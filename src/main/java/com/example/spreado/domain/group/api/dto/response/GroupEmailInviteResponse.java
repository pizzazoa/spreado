package com.example.spreado.domain.group.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "그룹 이메일 초대 발송 응답")
public record GroupEmailInviteResponse(
        @Schema(description = "그룹 ID", example = "1")
        Long groupId,

        @Schema(description = "초대 메일 발송 성공 개수", example = "3")
        int successCount,

        @Schema(description = "발송된 이메일 주소 목록")
        List<String> sentEmails
) {
}
