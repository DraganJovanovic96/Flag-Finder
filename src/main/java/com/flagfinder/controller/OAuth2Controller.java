package com.flagfinder.controller;

import com.flagfinder.dto.AuthenticationResponseDto;
import com.flagfinder.model.User;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;
    private final UserRepository userRepository;
    
    @Value("${spring.frontend.url}")
    private String frontendUrl;

    @GetMapping("/success")
    public void oauth2Success(@RequestParam String email, 
                             @RequestParam String name, 
                             @RequestParam String googleId,
                             @RequestParam(required = false) String picture,
                             @RequestParam(required = false) String givenName,
                             @RequestParam(required = false) String familyName,
                             @RequestParam(required = false) String locale,
                             HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("email", email);
            attributes.put("name", name);
            attributes.put("sub", googleId);
            
            if (picture != null) attributes.put("picture", picture);
            if (givenName != null) attributes.put("given_name", givenName);
            if (familyName != null) attributes.put("family_name", familyName);
            if (locale != null) attributes.put("locale", locale);
            
            
            OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
            );
            
            System.out.println("DEBUG - Processing OAuth2 user: " + email);
            AuthenticationResponseDto authResponse = oAuth2Service.processOAuth2User(oauth2User);
            System.out.println("DEBUG - OAuth2 processing completed, tokens generated");
            
            // Check if this is a new user (first time login) to show gameName customization
            User user = userRepository.findByEmail(email).orElse(null);
            boolean isNewUser = user != null && !user.isInitialSetupCompleted();
            System.out.println("DEBUG - Is new user (needs setup): " + isNewUser);
            System.out.println("DEBUG - Frontend URL: " + frontendUrl);
            
            if (isNewUser) {
                String redirectUrl = String.format("%s/setup-gamename?token=%s&refreshToken=%s&currentGameName=%s", 
                    frontendUrl, authResponse.getAccessToken(), authResponse.getRefreshToken(), 
                    java.net.URLEncoder.encode(user.getGameName(), "UTF-8"));
                System.out.println("DEBUG - Redirecting new user to: " + redirectUrl);
                response.sendRedirect(redirectUrl);
            } else {
                String redirectUrl = String.format("%s/oauth2/callback?token=%s&refreshToken=%s", 
                    frontendUrl, authResponse.getAccessToken(), authResponse.getRefreshToken());
                System.out.println("DEBUG - Redirecting existing user to: " + redirectUrl);
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            System.out.println("DEBUG - OAuth2 error: " + e.getMessage());
            e.printStackTrace();
            String errorUrl = String.format("%s/login?error=oauth2_failed", frontendUrl);
            response.sendRedirect(errorUrl);
        }
    }
}
