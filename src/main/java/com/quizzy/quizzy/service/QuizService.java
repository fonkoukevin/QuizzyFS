package com.quizzy.quizzy.service;

import com.quizzy.quizzy.dto.AnswerDTO;
import com.quizzy.quizzy.dto.QuestionDTO;
import com.quizzy.quizzy.entity.Answer;
import com.quizzy.quizzy.entity.Question;
import com.quizzy.quizzy.entity.Quiz;
import com.quizzy.quizzy.repository.QuestionRepository;
import com.quizzy.quizzy.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    /**
     * 🔥 [Issue 5] Récupérer tous les quiz d'un utilisateur
     */
    public List<Quiz> getQuizzesByUser(String ownerUid) {
        return quizRepository.findByOwnerUid(ownerUid);
    }

    /**
     * 🔥 [Issue 6] Créer un nouveau quiz
     */
    public Quiz createQuiz(String ownerUid, String title, String description) {
        Quiz quiz = new Quiz();
        quiz.setOwnerUid(ownerUid);
        quiz.setTitle(title);
        quiz.setDescription(description);
        return quizRepository.save(quiz);
    }

    /**
     * 🔥 [Issue 7] Récupérer un quiz par ID (uniquement si l'utilisateur en est le propriétaire)
     */
    public Optional<Quiz> getQuizById(String quizId, String ownerUid) {
        Optional<Quiz> quiz = quizRepository.findById(quizId);
        if (quiz.isPresent() && quiz.get().getOwnerUid().equals(ownerUid)) {
            return quiz;
        }
        return Optional.empty(); // Quiz non trouvé ou appartient à un autre utilisateur
    }

    public boolean updateQuizTitle(String quizId, String ownerUid, String newTitle) {
        Optional<Quiz> quizOptional = quizRepository.findById(quizId);

        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();

            if (!quiz.getOwnerUid().equals(ownerUid)) {
                logger.warn("🚫 Tentative de modification d'un quiz qui ne t'appartient pas !");
                return false; // L'utilisateur ne possède pas ce quiz
            }

            quiz.setTitle(newTitle);
            quizRepository.save(quiz);
            return true;
        }

        logger.warn("❌ Quiz ID {} non trouvé.", quizId);
        return false; // Quiz non trouvé
    }

    public Optional<Question> addQuestionToQuiz(String quizId, String text, List<Answer> answers) {
        Optional<Quiz> quizOptional = quizRepository.findById(quizId);

        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();

            Question question = new Question();
            question.setText(text);
            question.setQuiz(quiz);

            for (Answer answer : answers) {
                answer.setQuestion(question);
            }
            question.setAnswers(answers);

            Question savedQuestion = questionRepository.save(question);
            logger.info("✅ Question '{}' ajoutée au quiz '{}'", text, quiz.getTitle());
            return Optional.of(savedQuestion);
        }

        logger.error("❌ Quiz {} non trouvé", quizId);
        return Optional.empty();
    }

    public boolean updateQuestion(String quizId, Long questionId, QuestionDTO questionDTO) {
        Optional<Quiz> quizOptional = quizRepository.findById(quizId);
        if (quizOptional.isEmpty()) {
            logger.error("❌ Quiz {} non trouvé", quizId);
            return false;
        }

        Quiz quiz = quizOptional.get();

        // Vérifier que la question appartient bien au quiz
        Optional<Question> questionOptional = questionRepository.findById(questionId);
        if (questionOptional.isEmpty() || !questionOptional.get().getQuiz().equals(quiz)) {
            logger.error("❌ Question {} non trouvée dans le quiz {}", questionId, quizId);
            return false;
        }

        Question question = questionOptional.get();

        // Mise à jour du titre de la question
        question.setText(questionDTO.getTitle());

        // Suppression des réponses existantes et ajout des nouvelles
        question.getAnswers().clear();
        for (AnswerDTO answerDTO : questionDTO.getAnswers()) {
            Answer answer = new Answer();
            answer.setText(answerDTO.getTitle());
            answer.setCorrect(answerDTO.isCorrect());
            answer.setQuestion(question);
            question.getAnswers().add(answer);
        }

        // Sauvegarde de la question mise à jour
        questionRepository.save(question);
        logger.info("✅ Question {} mise à jour avec succès", questionId);

        return true;
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



}
