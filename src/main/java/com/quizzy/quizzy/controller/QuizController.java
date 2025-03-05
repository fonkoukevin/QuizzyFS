package com.quizzy.quizzy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizzy.quizzy.dto.AllQuizUserDTO;
import com.quizzy.quizzy.dto.QuestionDTO;
import com.quizzy.quizzy.dto.QuizDTO;
import com.quizzy.quizzy.dto.QuizUserDTO;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.SecureRandom;
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
    public ResponseEntity<AllQuizUserDTO> getUserQuizzes(
            @AuthenticationPrincipal Jwt jwt) throws JsonProcessingException {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        logger.info("✅ Retrieving quizzes for UID: {}", uid);

        AllQuizUserDTO AllQuizData = quizService.getQuizzesByUser(uid);

        return ResponseEntity.ok(AllQuizData);
    }


    /**
     * Endpoint pour démarrer un quiz (placeholder, à implémenter selon ton besoin).
     */

    @GetMapping("/{id}/start")
    public ResponseEntity<String> startQuiz(@PathVariable String id) {
        return ResponseEntity.ok("Quiz " + id + " started!");
    }


    @PostMapping("/{quizId}/start")
    public ResponseEntity<Void> startQuiz(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String quizId) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        logger.info("📌 Tentative de lancement du quiz {} par l'utilisateur {}", quizId, uid);

        // Vérifier que le quiz existe et appartient à l'utilisateur
        Optional<Quiz> quizOptional = quizService.getQuizById(quizId, uid);
        if (quizOptional.isEmpty()) {
            logger.error("❌ Quiz {} non trouvé ou n'appartient pas à l'utilisateur {}", quizId, uid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Quiz quiz = quizOptional.get();

        // Vérifier si le quiz est startable
        if (!quizService.isQuizStartable(quiz)) {
            logger.error("❌ Quiz {} ne peut pas être démarré car il ne respecte pas les critères", quizId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // Générer un ID unique pour l'exécution (6 caractères aléatoires)
        String executionId = generateExecutionId();

        // Enregistrer l'exécution dans le service (tu peux l'ajouter plus tard en base de données)
        quizService.createExecution(quizId, executionId);

        // Construire l'URL de l'exécution
        String executionUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/execution/{executionId}")
                .buildAndExpand(executionId)
                .toUriString();

        logger.info("✅ Exécution du quiz {} créée avec l'ID {}", quizId, executionId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", executionUrl)
                .build();
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
            @RequestBody QuizUserDTO quizUserDTO) {

        if (jwt == null) {
            logger.error("❌ JWT is null. Unauthorized request.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String uid = jwt.getSubject();
        if (quizUserDTO.title() == null || quizUserDTO.title().isEmpty()) {
            logger.error("❌ Title is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Quiz newQuiz = quizService.createQuiz(uid, quizUserDTO.title(), quizUserDTO.description());

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
