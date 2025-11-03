package com.example.spreado.global.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "에러 응답")
@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드", example = "500")
    private final int status;

    @Schema(description = "에러 메시지")
    private final String message;
}
