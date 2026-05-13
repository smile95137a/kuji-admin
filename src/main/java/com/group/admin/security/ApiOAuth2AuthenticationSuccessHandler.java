package com.group.admin.security;

import com.group.admin.config.AppUrlProperties;
import com.group.admin.res.AuthRes;
import com.group.admin.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final AppUrlProperties appUrlProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub");
        String picture = oAuth2User.getAttribute("picture");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank() || googleId == null || googleId.isBlank()) {
            log.warn("⚠️ OAuth2 成功回呼缺少必要屬性: email={}, sub={}", email, googleId);
            redirectFailure(response, "OAUTH2_PROFILE_INVALID", "Google 個人資料不完整");
            return;
        }

        try {
            AuthRes authRes = userService.loginWithGoogleProfile(email, googleId, picture, name);

            String targetUrl = UriComponentsBuilder
                    .fromHttpUrl(appUrlProperties.getClientOAuth2CallbackUrl())
                    .queryParam("accessToken", authRes.getAccessToken())
                    .queryParam("refreshToken", authRes.getRefreshToken())
                    .queryParam("expiresIn", authRes.getExpiresIn())
                    .queryParam("isNewUser", authRes.getIsNewUser())
                    .build(true)
                    .toUriString();

            response.sendRedirect(targetUrl);
        } catch (Exception ex) {
            log.error("❌ OAuth2 成功處理失敗", ex);
            redirectFailure(response, "OAUTH2_LOGIN_FAILED", "Google 登入失敗");
        }
    }

    private void redirectFailure(HttpServletResponse response, String code, String message) throws IOException {
        String targetUrl = UriComponentsBuilder
                .fromHttpUrl(appUrlProperties.getClientOAuth2CallbackUrl())
                .queryParam("error", code)
                .queryParam("message", message)
                .build(true)
                .toUriString();
        response.sendRedirect(targetUrl);
    }
}
