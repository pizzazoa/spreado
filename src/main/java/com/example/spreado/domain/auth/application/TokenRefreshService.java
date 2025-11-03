package com.example.spreado.domain.auth.application;

import com.example.spreado.domain.auth.core.entity.UserRefreshToken;
import com.example.spreado.domain.auth.core.repository.TokenRefreshRepository;
import com.example.spreado.domain.user.application.UserService;
import com.example.spreado.domain.user.core.entity.User;
import com.example.spreado.global.security.token.AccessTokenProvider;
import com.example.spreado.global.security.token.RefreshTokenProvider;
import com.example.spreado.global.shared.exception.JwtAuthException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenRefreshService {

    private final TokenRefreshRepository tokenRefreshRepository;
    private final RefreshTokenProvider refreshTokenProvider;
    private final AccessTokenProvider accessTokenProvider;
    private final UserService userService;

    /**
     * 로그인 시 refresh token 해시 저장 (기존 토큰 무효화)
     */
    public void saveUserRefreshToken(Long userId, String hashedRefreshToken) {
        User user = userService.getUserById(userId);

        // 기존 토큰들 revoke
        tokenRefreshRepository.deleteByUserId(userId);

        // 새 토큰 저장
        UserRefreshToken newToken = UserRefreshToken.create(
                user,
                hashedRefreshToken,
                RefreshTokenProvider.validDuration
        );

        tokenRefreshRepository.save(newToken);
    }

    /**
     * 재발급 요청 시 refresh token 검증 및 새로운 access token 발급
     */
    @Transactional(readOnly = true)
    public String reissueAccessToken(String rawRefreshToken) {
        String hashed = refreshTokenProvider.hash(rawRefreshToken);

        UserRefreshToken savedToken = tokenRefreshRepository.findByRefreshTokenHash(hashed)
                .orElseThrow(() -> new JwtAuthException("유효하지 않은 리프레시 토큰입니다."));

        if (!savedToken.isUsable()) {
            throw new JwtAuthException("만료되었거나 폐기된 리프레시 토큰입니다.");
        }

        Long userId = savedToken.getUser().getId();
        return accessTokenProvider.createAccessToken(userId);
    }

    /**
     * 로그아웃 시 refresh token 폐기
     */
    public void revokeRefreshToken(Long userId) {
        tokenRefreshRepository.deleteByUserId(userId);
    }

    /**
     * 특정 raw 리프레시 토큰 무효화 (선택적으로)
     */
    public void revokeByRawToken(String rawRefreshToken) {
        String hashed = refreshTokenProvider.hash(rawRefreshToken);
        tokenRefreshRepository.findByRefreshTokenHash(hashed)
                .ifPresent(UserRefreshToken::revoke);
    }
}
