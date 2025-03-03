package com.quizzy.quizzy.controller;

import com.quizzy.quizzy.dto.AnswerDTO;
import com.quizzy.quizzy.dto.QuestionDTO;
import com.quizzy.quizzy.dto.QuizDTO;
import com.quizzy.quizzy.dto.QuizDetailsDTO;
import com.quizzy.quizzy.entity.Answer;
import com.quizzy.quizzy.entity.Question;
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
     * 🔥 [Issue 7] Récupérer un quiz par son ID (seulement si l'utilisateur en est propriétaire)
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailsDTO> getQuizById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        Optional<Quiz> quizOptional = quizService.getQuizById(id, uid);

        if (quizOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Quiz quiz = quizOptional.get();

        // Convertir en DTO
        QuizDetailsDTO quizDTO = new QuizDetailsDTO();
        quizDTO.setTitle(quiz.getTitle());
        quizDTO.setDescription(quiz.getDescription());

        // Mapper les questions
        List<QuestionDTO> questionDTOS = quiz.getQuestions().stream().map(question -> {
            QuestionDTO qDto = new QuestionDTO();
            qDto.setTitle(question.getText());
            qDto.setAnswers(question.getAnswers().stream().map(answer -> {
                AnswerDTO aDto = new AnswerDTO();
                aDto.setTitle(answer.getText());
                aDto.setCorrect(answer.isCorrect());
                return aDto;
            }).toList());
            return qDto;
        }).toList();

        quizDTO.setQuestions(questionDTOS);

        return ResponseEntity.ok(quizDTO);
    }

    @PostMapping
    public ResponseEntity<Void> createQuiz(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) QuizDTO quizDTO) { // Permet de gérer un body vide

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();

        // ✅ Vérification si quizDTO est null et ajout des valeurs par défaut
        if (quizDTO == null) {
            quizDTO = new QuizDTO();
        }
        String title = (quizDTO.getTitle() == null || quizDTO.getTitle().isEmpty()) ? "My title" : quizDTO.getTitle();
        String description = (quizDTO.getDescription() == null || quizDTO.getDescription().isEmpty()) ? "my description" : quizDTO.getDescription();

        logger.info("📩 Création d'un quiz avec Title: '{}' et Description: '{}'", title, description);

        Quiz newQuiz = quizService.createQuiz(uid, title, description);

        String location = String.format("/api/quiz/%s", newQuiz.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", location)
                .build();
    }



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

        // Vérifier si la requête est bien un tableau JSON et qu'il contient au moins un élément
        if (updates == null || updates.isEmpty()) {
            logger.error("❌ No update operations provided.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Extraire la première opération (on suppose une seule opération de mise à jour à la fois)
        Map<String, String> updateOperation = updates.get(0);

        if (!"replace".equals(updateOperation.get("op")) || !"/title".equals(updateOperation.get("path"))) {
            logger.error("❌ Invalid patch request format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Récupérer l'ancien quiz pour obtenir son titre actuel
        Optional<Quiz> quizOptional = quizService.getQuizById(id, uid);
        if (quizOptional.isEmpty()) {
            logger.error("❌ Quiz {} non trouvé ou n'appartient pas à l'utilisateur", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Quiz quiz = quizOptional.get();

        // Si "value" est absent ou vide, on garde l'ancien titre
        String newTitle = updateOperation.get("value");
        if (newTitle == null || newTitle.trim().isEmpty()) {
            newTitle = quiz.getTitle(); // Ne pas modifier si vide
            logger.warn("⚠️ Aucun nouveau titre fourni, conservation de l'ancien titre.");
        }

        // Mise à jour du titre
        boolean updated = quizService.updateQuizTitle(id, uid, newTitle);

        if (!updated) {
            logger.error("❌ Quiz {} non trouvé ou appartient à un autre utilisateur", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        logger.info("✅ Quiz title updated successfully to '{}'", newTitle);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<Void> addQuestionToQuiz(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody QuestionDTO questionDTO) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();

        // LOG pour voir les données envoyées
        logger.info("📩 Réception de la question pour le quiz {} : {}", id, questionDTO);

        if (questionDTO.getTitle() == null) {
            logger.error("❌ Le titre de la question est manquant");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Vérifier que le quiz appartient à l'utilisateur
        Optional<Quiz> quizOptional = quizService.getQuizById(id, uid);
        if (quizOptional.isEmpty()) {
            logger.error("❌ Quiz {} non trouvé ou n'appartient pas à l'utilisateur", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Si la liste des réponses est vide, on ajoute des réponses par défaut
        List<Answer> answers;
        if (questionDTO.getAnswers() == null || questionDTO.getAnswers().isEmpty()) {
            logger.warn("⚠️ Aucune réponse fournie, génération automatique...");
            answers = List.of(
                    new Answer("Answer 1", false),
                    new Answer("Answer 2", false),
                    new Answer("Answer 3", false),
                    new Answer("Answer 4", false)
            );
        } else {
            answers = questionDTO.getAnswers().stream()
                    .map(dto -> new Answer(dto.getTitle(), dto.isCorrect()))
                    .toList();
        }

        Optional<Question> questionOptional = quizService.addQuestionToQuiz(id, questionDTO.getTitle(), answers);

        if (questionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String location = String.format("/api/quiz/%s/questions/%d", id, questionOptional.get().getId());
        return ResponseEntity.status(HttpStatus.CREATED).header("Location", location).build();
    }


    @PutMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<Void> updateQuestion(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String quizId,
            @PathVariable Long questionId,
            @RequestBody QuestionDTO questionDTO) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();

        // Vérifier que le quiz appartient à l'utilisateur
        Optional<Quiz> quizOptional = quizService.getQuizById(quizId, uid);
        if (quizOptional.isEmpty()) {
            logger.error("❌ Quiz {} non trouvé ou n'appartient pas à l'utilisateur", quizId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Vérifier que la question appartient bien au quiz
        boolean updated = quizService.updateQuestion(quizId, questionId, questionDTO);

        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        logger.info("✅ Question {} mise à jour dans le quiz {}", questionId, quizId);
        return ResponseEntity.noContent().build();
    }


}
