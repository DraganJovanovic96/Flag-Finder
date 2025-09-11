package com.flagfinder.controller;

import com.flagfinder.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @GetMapping("/success")
    public void oauth2Success(@RequestParam String email, 
                             @RequestParam String name, 
                             @RequestParam String googleId,
                             @RequestParam(required = false) String picture,
                             @RequestParam(required = false) String givenName,
                             @RequestParam(required = false) String familyName,
                             @RequestParam(required = false) String locale,
                             HttpServletResponse response) throws IOException {
        Map<String, String> userParams = new HashMap<>();
        userParams.put("email", email);
        userParams.put("name", name);
        userParams.put("googleId", googleId);
        userParams.put("picture", picture);
        userParams.put("givenName", givenName);
        userParams.put("familyName", familyName);
        userParams.put("locale", locale);
        
        oAuth2Service.handleOAuth2Success(userParams, response);
    }
}
