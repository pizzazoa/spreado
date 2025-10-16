package com.example.spreado.domain.auth.api.dto;

public record AuthTokensResponse(
        String accessToken,
        String refreshToken
) {}
