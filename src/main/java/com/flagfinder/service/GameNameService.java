package com.flagfinder.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface GameNameService {
    ResponseEntity<?> setGameName(Map<String, String> request, Authentication authentication);
    ResponseEntity<?> checkGameNameAvailability(String gameName);
}
