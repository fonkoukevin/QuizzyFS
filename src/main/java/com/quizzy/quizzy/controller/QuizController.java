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
import java.util.Optional;
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

    /**
     * 🔥 [Issue 5] Récupérer tous les quiz d'un utilisateur
     */
    @GetMapping
    public ResponseEntity<Map<String, List<Map<String, String>>>> getUserQuizzes(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        logger.info("✅ Retrieving quizzes for UID: {}", uid);

        List<Quiz> quizzes = quizService.getQuizzesByUser(uid);

        List<Map<String, String>> quizData = quizzes.stream()
                .map(quiz -> Map.of(
                        "id", quiz.getId(),
                        "title", quiz.getTitle(),
                        "description", quiz.getDescription()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("data", quizData));
    }

    /**
     * 🔥 [Issue 6] Création d'un nouveau quiz
     */
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

        Quiz newQuiz = quizService.createQuiz(uid, title, description);

        String location = String.format("/api/quiz/%s", newQuiz.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", location)
                .build();
    }

    /**
     * 🔥 [Issue 7] Récupérer un quiz par son ID (seulement si l'utilisateur en est propriétaire)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQuizById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        logger.info("🔍 Retrieving quiz {} for user {}", id, uid);

        Optional<Quiz> quizOptional = quizService.getQuizById(id, uid);

        if (quizOptional.isEmpty()) {
            logger.error("❌ Quiz not found or does not belong to user.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Quiz quiz = quizOptional.get();
        Map<String, Object> response = Map.of(
                "title", quiz.getTitle(),
                "description", quiz.getDescription(),
                "questions", List.of() // À remplacer par une vraie liste de questions si implémenté
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 [Issue 8] Mettre à jour le titre d'un quiz
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuizTitle(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody List<Map<String, String>> updates) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        logger.info("🔄 Updating quiz title for UID: {}, Quiz ID: {}", uid, id);

        if (updates.isEmpty() || !updates.get(0).get("op").equals("replace") ||
                !updates.get(0).get("path").equals("/title") || updates.get(0).get("value") == null) {
            logger.error("❌ Invalid patch request format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String newTitle = updates.get(0).get("value");

        boolean updated = quizService.updateQuizTitle(id, uid, newTitle);

        if (!updated) {
            logger.error("❌ Quiz not found or does not belong to user.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        logger.info("✅ Quiz title updated successfully.");
        return ResponseEntity.noContent().build();
    }
}
