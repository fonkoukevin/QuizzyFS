package com.quizzy.quizzy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class PingController {

    private final JdbcTemplate jdbcTemplate;

    public PingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> details = new HashMap<>();

        try {

            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            details.put("database", "OK");
            response.put("status", "OK");
        } catch (Exception e) {
            details.put("database", "KO");
            response.put("status", "Partial");
        }

        response.put("details", details);

        HttpStatus status = details.get("database").equals("OK") ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }
}
