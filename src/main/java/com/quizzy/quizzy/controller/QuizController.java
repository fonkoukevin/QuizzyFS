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
                .map(quiz -> Map.of(
                        "id", quiz.getId(),
                        "title", quiz.getTitle(),
                        "description", quiz.getDescription() // Ajout de la description dans la réponse
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("data", quizData));
    }

    @PostMapping
    public ResponseEntity<Void> createQuiz(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> quizData) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        String title = quizData.get("title");
        String description = quizData.get("description");

        if (title == null || title.isEmpty()) {
            logger.error("❌ Title is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // ✅ Create quiz
        Quiz newQuiz = quizService.createQuiz(uid, title, description);

        // ✅ Construct the Location header
        String location = String.format("/api/quiz/%s", newQuiz.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", location)
                .build();
    }
}
