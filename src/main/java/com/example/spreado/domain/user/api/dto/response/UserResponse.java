package com.example.spreado.domain.user.api.dto.response;

import com.example.spreado.domain.user.core.entity.User;

public record UserResponse(
    Long id,
    String email,
    String nickname
) {
    public UserResponse(User user) {
        this(
            user.getId(),
            user.getEmail(),
            user.getName()
        );
    }
}
