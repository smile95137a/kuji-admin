package com.group.admin.security;

import com.group.admin.config.AppUrlProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final AppUrlProperties appUrlProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.warn("⚠️ OAuth2 驗證失敗: {}", exception.getMessage());

        String targetUrl = UriComponentsBuilder
                .fromHttpUrl(appUrlProperties.getClientOAuth2CallbackUrl())
                .queryParam("error", "OAUTH2_AUTHENTICATION_FAILED")
                .queryParam("message", exception.getMessage())
                .build(true)
                .toUriString();

        response.sendRedirect(targetUrl);
    }
}
