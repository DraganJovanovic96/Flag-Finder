package com.flagfinder.service;

import com.flagfinder.dto.AuthenticationResponseDto;
import org.springframework.security.oauth2.core.user.OAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface OAuth2Service {
    AuthenticationResponseDto processOAuth2User(OAuth2User oauth2User);
    void handleOAuth2Success(Map<String, String> userParams, HttpServletResponse response) throws IOException;
}
