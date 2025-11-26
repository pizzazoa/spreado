package com.example.spreado.domain.group.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "초대 링크로 조회한 그룹 정보")
public record GroupInviteInfoResponse(
        @Schema(description = "그룹 ID", example = "1")
        Long groupId,

        @Schema(description = "그룹 이름", example = "스프레도 개발팀")
        String groupName,

        @Schema(description = "그룹 멤버 수", example = "5")
        int memberCount,

        @Schema(description = "그룹 리더 이름", example = "홍길동")
        String leaderName,

        @Schema(description = "초대 링크", example = "https://spreado-24767338797.asia-northeast3.run.app/abc123")
        String inviteLink
) {
}
