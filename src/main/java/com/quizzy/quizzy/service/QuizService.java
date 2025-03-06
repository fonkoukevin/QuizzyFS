package com.quizzy.quizzy.service;

import com.quizzy.quizzy.dto.*;
import com.quizzy.quizzy.entity.Answer;
import com.quizzy.quizzy.entity.Question;
import com.quizzy.quizzy.entity.Quiz;
import com.quizzy.quizzy.repository.QuestionRepository;
import com.quizzy.quizzy.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ConcurrentMap<String, String> executions = new ConcurrentHashMap<>();

    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    public boolean isQuizStartable(Quiz quiz) {
        // Vérifie que le titre n'est pas vide
        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            return false;
        }

        // Vérifie qu'il y a au moins une question
        if (quiz.getQuestions().isEmpty()) {
            return false;
        }

        // Vérifie que toutes les questions sont valides
        for (Question question : quiz.getQuestions()) {
            if (question.getText() == null || question.getText().trim().isEmpty()) {
                return false;
            }
            if (question.getAnswers().size() < 2) {
                return false;
            }
            long correctAnswers = question.getAnswers().stream().filter(Answer::isCorrect).count();
            if (correctAnswers != 1) {
                return false;
            }
        }

        return true;
    }

    public void createExecution(String quizId, String executionId) {
        executions.put(executionId, quizId);
        logger.info("🔹 Exécution enregistrée : executionId={} pour quizId={}", executionId, quizId);

    }



    public AllQuizUserDTO getQuizzesByUser(String ownerUid) {
        List<QuizUserDTO> quizzes = quizRepository.findByOwnerUid(ownerUid).stream()
                .map(quiz -> new QuizUserDTO(
                        String.valueOf(quiz.getId()),
                        quiz.getTitle(),
                        quiz.getDescription(),
                        isQuizStartable(quiz) ? Map.of("start", getBaseUrl() + "/api/quiz/" + quiz.getId() + "/start") : Map.of()
                ))
                .collect(Collectors.toList());

        return new AllQuizUserDTO(quizzes, Map.of("create", getBaseUrl() + "/api/quiz"));
    }


    public Quiz createQuiz(String ownerUid, String title, String description) {
        return quizRepository.save(new Quiz(ownerUid, title, description));
    }


    private String getBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    public Optional<Quiz> getQuizEntityById(String quizId, String ownerUid) {
        return quizRepository.findById(quizId)
                .filter(quiz -> quiz.getOwnerUid().equals(ownerUid));
    }

        public Optional<QuizDetailsDTO> getQuizById(String quizId, String ownerUid) {
            return quizRepository.findById(quizId)
                    .filter(quiz -> quiz.getOwnerUid().equals(ownerUid))
                    .map(quiz -> new QuizDetailsDTO(
                            quiz.getTitle(),
                            quiz.getDescription(),
                            quiz.getQuestions().stream()
                                    .map(q -> new QuestionDTO(String.valueOf(q.getId()), q.getText(), q.getAnswers().stream()
                                            .map(a -> new AnswerDTO(a.getText(), a.isCorrect()))
                                            .toList()))
                                    .toList()
                    ));
        }

        public boolean updateQuizTitle(String quizId, String ownerUid, List<Map<String, String>> updates) {
            return quizRepository.findById(quizId)
                    .filter(quiz -> quiz.getOwnerUid().equals(ownerUid))
                    .map(quiz -> {
                        quiz.setTitle(updates.get(0).get("value"));
                        quizRepository.save(quiz);
                        return true;
                    })
                    .orElse(false);
        }

        public Optional<Question> addQuestionToQuiz(String quizId, String ownerUid, QuestionDTO questionDTO) {
            return quizRepository.findById(quizId)
                    .filter(quiz -> quiz.getOwnerUid().equals(ownerUid))
                    .map(quiz -> {
                        Question question = new Question(questionDTO.title(), quiz);
                        List<Answer> answers = questionDTO.answers().stream()
                                .map(dto -> new Answer(dto.title(), dto.isCorrect(), question))
                                .collect(Collectors.toList()); // ✅ Collecte en tant que List<Answer>

                        question.setAnswers(answers); // ✅ Pas d'erreur de typage




                        return questionRepository.save(question);
                    });
        }

        public boolean updateQuestion(String quizId, String questionId, String ownerUid, QuestionDTO questionDTO) {
            return questionRepository.findById(Long.parseLong(questionId))
                    .filter(question -> question.getQuiz().getId().equals(quizId))
                    .map(question -> {
                        question.setText(questionDTO.title());
                        List<Answer> answers = question.getAnswers();
                        answers.clear(); // ✅ Supprime les anciennes réponses sans casser la collection Hibernate

                        for (AnswerDTO answerDTO : questionDTO.answers()) {
                            Answer answer = new Answer();
                            answer.setText(answerDTO.title());
                            answer.setCorrect(answerDTO.isCorrect());
                            answer.setQuestion(question);
                            answers.add(answer); // ✅ Ajoute directement dans la collection existante
                        }

// Hibernate détectera les suppressions/ajouts sans problème
                        questionRepository.save(question);

                        questionRepository.save(question);
                        return true;
                    })
                    .orElse(false);
        }

        public Optional<URI> startQuiz(String quizId, String ownerUid) {
            return quizRepository.findById(quizId)
                    .filter(quiz -> quiz.getOwnerUid().equals(ownerUid) && isQuizStartable(quiz))
                    .map(quiz -> {
                        String executionId = generateExecutionId();
                        executions.put(executionId, quizId);
                        return URI.create("/execution/" + executionId);
                    });
        }

        private String generateExecutionId() {
            return new SecureRandom().ints(6, 0, 36)
                    .mapToObj(i -> "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(i))
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        }


    public URI getQuizLocation(String quizId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/quiz/{id}")
                .buildAndExpand(quizId)
                .toUri();
    }

    // Méthode pour générer l'URL d'une question
    public URI getQuestionLocation(String quizId, String questionId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/quiz/{quizId}/questions/{questionId}")
                .buildAndExpand(quizId, questionId)
                .toUri();
    }




}
