package com.lms.library.infrastructure.security;

import com.lms.library.application.service.LoginHistoryService;
import com.lms.library.application.service.RefreshTokenService;
import com.lms.library.domain.entity.LoginHistory;
import com.lms.library.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final LoginHistoryService loginHistoryService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("OAuth2 login successful");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // Tạo JWT Access Token
        String accessToken = jwtUtil.generateToken(user);

        // Tạo Refresh Token
        String userAgent = request.getHeader("User-Agent");
        String refreshToken = refreshTokenService.createRefreshToken(user, userAgent).getToken();

        // Ghi nhận lịch sử đăng nhập
        loginHistoryService.saveLogin(user, request,
                LoginHistory.LoginType.GOOGLE,
                LoginHistory.LoginStatus.SUCCESS);

        // Redirect về Frontend kèm tokens
        String redirectUrl = frontendUrl + "/oauth2/redirect"
                + "?token=" + accessToken
                + "&refreshToken=" + refreshToken;

        log.info("Redirecting OAuth2 successful user to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
