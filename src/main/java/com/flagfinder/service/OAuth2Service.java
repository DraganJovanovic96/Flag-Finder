package com.flagfinder.service;

import com.flagfinder.dto.AuthenticationResponseDto;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2Service {
    AuthenticationResponseDto processOAuth2User(OAuth2User oauth2User);
}
