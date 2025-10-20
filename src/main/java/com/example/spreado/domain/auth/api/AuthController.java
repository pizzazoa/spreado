package com.example.spreado.domain.auth.api;

import com.example.spreado.domain.auth.api.dto.response.AccessTokenResponse;
import com.example.spreado.domain.auth.application.TokenRefreshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "소셜 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final TokenRefreshService tokenRefreshService;

    @PostMapping("/refresh")
    @Operation(
            summary = "로그인 상태 유지/자동 로그인 (액세스 토큰 재발급)",
            description = """
    클라이언트(웹)의 리프레시 토큰을 사용해 새로운 액세스 토큰을 발급받는 엔드포인트입니다.
    
    • **응답(200):** JSON 형식으로 새 액세스 토큰(`accessToken`)이 반환됩니다.
    • **오류(401):** 리프레시 토큰이 없거나 유효하지 않을 경우 401 응답이 반환되며, 이 경우 FE에서는 로그인 화면으로 이동해야 합니다.
    
    ※ 리프레시 토큰은 30일간 유효하며, 이 API를 호출할 때마다 서버가 새 리프레시 토큰으로 쿠키를 갱신해 유효 기간을 연장합니다.
    즉, 30일 동안 한 번도 호출하지 않으면 만료되어 재로그인이 필요합니다.
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 갱신 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AccessTokenResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "리프레시 토큰이 없거나 유효하지 않음"
                    )
            }
    )
    public AccessTokenResponse refresh(@RequestParam String refreshToken) {
        String newAccessToken = tokenRefreshService.reissueAccessToken(refreshToken);
        return new AccessTokenResponse(newAccessToken);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "로그아웃",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
                    리프레시 토큰 삭제를 통한 로그아웃 처리입니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            }
    )
    public void logout(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        tokenRefreshService.revokeRefreshToken(userId);
    }
}
