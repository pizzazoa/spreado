package com.example.spreado.global.security;

import com.example.spreado.domain.auth.api.dto.AuthTokensResponse;
import com.example.spreado.domain.user.application.UserService;
import com.example.spreado.domain.user.core.entity.User;
import com.example.spreado.global.security.token.AccessTokenProvider;
import com.example.spreado.global.security.token.RefreshTokenPair;
import com.example.spreado.global.security.token.RefreshTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 사용자 등록 or 조회
        User user = userService.findOrCreateUser(email, name);

        // Access + Refresh 발급
        String accessToken = accessTokenProvider.createAccessToken(user.getId());
        RefreshTokenPair refreshPair = refreshTokenProvider.generateRefreshTokenPair();

        // hashed RefreshToken DB 저장
        // 보안상 raw token은 클라이언트에게만 반환
        userService.storeHashedRefreshToken(user.getId(), refreshPair.hashed());

        // 응답 작성
        AuthTokensResponse tokens = new AuthTokensResponse(accessToken, refreshPair.raw());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(tokens));
    }
}

