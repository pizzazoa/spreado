package com.example.spreado.domain.liveblocks.api;

import com.example.spreado.domain.liveblocks.application.LiveblocksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Liveblocks API", description = "Liveblocks 토큰 발급 API")
@RestController
@RequestMapping("/liveblocks")
@RequiredArgsConstructor
public class LiveblocksController {

    private final LiveblocksService liveblocksService;

    @PostMapping("/{meetingId}")
    @Operation(
            summary = "Liveblocks 토큰 발급",
            description = "Liveblocks 실시간 협업을 위한 액세스 토큰을 발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
            @ApiResponse(responseCode = "401", description = "사용자 인증 실패"),
            @ApiResponse(responseCode = "404", description = "해당 미팅을 찾을 수 없음")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getToken(
            @PathVariable Long meetingId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> tokenJson = liveblocksService.getToken(meetingId, userId);

        return ResponseEntity.ok(tokenJson);
    }
}
