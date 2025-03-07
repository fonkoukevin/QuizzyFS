package com.quizzy.quizzy.controller;


import com.quizzy.quizzy.dto.*;
import com.quizzy.quizzy.entity.Quiz;
import com.quizzy.quizzy.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.SecureRandom;


import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Endpoint pour démarrer un quiz (placeholder, à implémenter selon ton besoin).
     */
    @GetMapping("/{id}/start")
    public ResponseEntity<String> startQuiz(@PathVariable String id) {
        return ResponseEntity.ok("Quiz " + id + " started!");
    }


    /**
     * Génère un ID aléatoire de 6 caractères pour l'exécution du quiz.
     */
    private String generateExecutionId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }



        @GetMapping
        public ResponseEntity<AllQuizUserDTO> getUserQuizzes(JwtAuthenticationToken jwt) {
            return ResponseEntity.ok(quizService.getQuizzesByUser(jwt.getName()));
        }

        @GetMapping("/{id}")
        public ResponseEntity<QuizDetailsDTO> getQuizById(JwtAuthenticationToken jwt, @PathVariable String id) {
            return quizService.getQuizById(id, jwt.getName())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

    @PostMapping
    public ResponseEntity<Void> createQuiz(JwtAuthenticationToken jwt, @RequestBody QuizDTO quizDTO) {
        var quiz = quizService.createQuiz(jwt.getName(), quizDTO.title(), quizDTO.description());
        return ResponseEntity.created(quizService.getQuizLocation(quiz.getId())).build();
    }

    @PatchMapping("/{id}")
        public ResponseEntity<Void> updateQuizTitle(@AuthenticationPrincipal Jwt jwt, @PathVariable String id, @RequestBody List<Map<String, String>> updates) {
            return quizService.updateQuizTitle(id, jwt.getSubject(), updates) ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        }
    @PostMapping("/{id}/questions")
    public ResponseEntity<Void> addQuestionToQuiz(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable String id,
                                                  @RequestBody QuestionDTO questionDTO) {
        return quizService.addQuestionToQuiz(id, jwt.getSubject(), questionDTO)
                .<ResponseEntity<Void>>map(question -> {
                    URI location = quizService.getQuestionLocation(id, String.valueOf(question.getId()));
                    return ResponseEntity.created(location).build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping("/{quizId}/questions/{questionId}")
        public ResponseEntity<Void> updateQuestion(@AuthenticationPrincipal Jwt jwt, @PathVariable String quizId, @PathVariable String questionId, @RequestBody QuestionDTO questionDTO) {
            return quizService.updateQuestion(quizId, questionId, jwt.getSubject(), questionDTO) ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        }

    @PostMapping("/{quizId}/start")
    public ResponseEntity<Void> startQuiz(@AuthenticationPrincipal Jwt jwt, @PathVariable String quizId) {
        return quizService.startQuiz(quizId, jwt.getSubject())
                .<ResponseEntity<Void>>map(location -> ResponseEntity.created(location).build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

//    @GetMapping("/{quizId}/execution")
//    public ResponseEntity<Map<String, String>> getExecutionId(@PathVariable String quizId) {
//        Optional<Quiz> quiz = quizService.getQuizById(quizId);
//        if (quiz.isEmpty() || quiz.get().getExecutionId() == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No executionId found"));
//        }
//        return ResponseEntity.ok(Map.of("executionId", quiz.get().getExecutionId()));
//    }


}
