package com.quizzy.quizzy.service;

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

@Service
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

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

    /**
     * 🔥 [Issue 8] Mettre à jour le titre d'un quiz
     */
    public boolean updateQuizTitle(String quizId, String ownerUid, String newTitle) {
        Optional<Quiz> quizOptional = quizRepository.findById(quizId);

        if (quizOptional.isPresent()) {
            Quiz quiz = quizOptional.get();

            if (!quiz.getOwnerUid().equals(ownerUid)) {
                logger.warn("🚫 Tentative de modification d'un quiz ne appartenant pas à l'utilisateur !");
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
}
