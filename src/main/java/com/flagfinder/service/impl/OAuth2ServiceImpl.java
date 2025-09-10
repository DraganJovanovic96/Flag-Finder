package com.flagfinder.service.impl;

import com.flagfinder.dto.AuthenticationResponseDto;
import com.flagfinder.model.Token;
import com.flagfinder.model.User;
import com.flagfinder.repository.TokenRepository;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.impl.JwtService;
import com.flagfinder.service.OAuth2Service;
import com.flagfinder.enumeration.Role;
import com.flagfinder.enumeration.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    @Override
    public AuthenticationResponseDto processOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String pictureUrl = oauth2User.getAttribute("picture");
        
        
        if (firstName == null && name != null && name.contains(" ")) {
            String[] nameParts = name.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts[1];
        } else if (firstName == null && name != null) {
            firstName = name;
        }


        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByEmail(email);
        }
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            boolean needsUpdate = false;
            
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                needsUpdate = true;
            }
            if (firstName != null && !firstName.equals(user.getFirstname())) {
                user.setFirstname(firstName);
                needsUpdate = true;
            }
            
            if (lastName != null && !lastName.equals(user.getLastname())) {
                user.setLastname(lastName);
                needsUpdate = true;
            }
            
            if (pictureUrl != null && !pictureUrl.equals(user.getImageUrl())) {
                user.setImageUrl(pictureUrl);
                needsUpdate = true;
            }
            
            
            if (needsUpdate) {
                userRepository.save(user);
            }
        } else {
            String uniqueGameName = generateUniqueGameName(name);
            
            user = User.builder()
                    .email(email)
                    .gameName(uniqueGameName)
                    .firstname(firstName)
                    .lastname(lastName)
                    .imageUrl(pictureUrl)
                    .googleId(googleId)
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("gameName", user.getGameName());
        
        String jwtToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private String generateUniqueGameName(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "Player";
        }
        
        String baseName = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (baseName.length() > 15) {
            baseName = baseName.substring(0, 15);
        }
        
        String gameName = baseName;
        int counter = 1;
        
        while (userRepository.findOneByGameNameIgnoreCase(gameName).isPresent()) {
            gameName = baseName + counter;
            counter++;
        }
        
        return gameName;
    }
}
