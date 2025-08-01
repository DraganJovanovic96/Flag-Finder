package com.flagfinder.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling ping requests.
 * This controller provides a response of 200 OK to pinging service.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ping")
@CrossOrigin
public class PingController {

    /**
     * Ping endpoint to keep the service alive.
     *
     * @return ResponseEntity with HTTP 200 status.
     */
    @GetMapping
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Service is running");
    }
}
