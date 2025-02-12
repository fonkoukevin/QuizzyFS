package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.entity.Quiz;
import com.quizzy.quizzy.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<Map<String, String>>>> getUserQuizzes(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            logger.error("❌ JWT is null. The request is unauthorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ Extract user ID from Firebase token
        String uid = jwt.getSubject();
        logger.info("✅ Retrieving quizzes for UID: {}", uid);

        // ✅ Fetch user quizzes
        List<Quiz> quizzes = quizService.getQuizzesByUser(uid);

        // ✅ Format response
        List<Map<String, String>> quizData = quizzes.stream()
                .map(quiz -> Map.of("id", quiz.getId(), "title", quiz.getTitle()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("data", quizData));
    }
}
