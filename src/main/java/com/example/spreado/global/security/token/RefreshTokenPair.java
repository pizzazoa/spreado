package com.example.spreado.global.security.token;

public record RefreshTokenPair(
        String raw,
        String hashed
) {
}
