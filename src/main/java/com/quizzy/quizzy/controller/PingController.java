package com.quizzy.quizzy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class PingController {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    public PingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> details = new HashMap<>();

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        try {
            // 🔹 Vérifier si la base de données répond
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", "OK");
            response.put("status", "OK");
        } catch (Exception e) {
            details.put("database", "KO");
            response.put("status", "Partial"); // 🔥 Indique que seule la DB est en panne
        }

        response.put("details", details);

        HttpStatus status = details.get("database").equals("OK") ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }
}
