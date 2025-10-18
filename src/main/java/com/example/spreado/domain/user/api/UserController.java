package com.example.spreado.domain.user.api;

import com.example.spreado.domain.user.application.UserService;
import com.example.spreado.domain.user.core.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "회원 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 회원 정보 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            나의 회원 정보를 조회합니다.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "회원 인증 실패",
                            content = @Content
                    ),
            }
    )
    public User getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "ID로 회원 조회",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "회원 없음",
                            content = @Content
                    )
            }
    )
    public User getUserInfo(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
