package com.quizzy.quizzy.service;

import com.quizzy.quizzy.entity.Quiz;
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

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
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
    public Optional<Quiz> getQuizWithQuestionsById(String quizId, String ownerUid) {
        Optional<Quiz> quiz = quizRepository.findById(quizId);

        if (quiz.isPresent() && quiz.get().getOwnerUid().equals(ownerUid)) {
            Quiz fetchedQuiz = quiz.get();
            fetchedQuiz.getQuestions().size(); // 🔥 Force le chargement des questions si nécessaire (Lazy Fetch)
            return Optional.of(fetchedQuiz);
        }
        return Optional.empty();
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
            quizRepository.save
                    (quiz);
            return true;
        }

        logger.warn("❌ Quiz ID {} non trouvé.", quizId);
        return false; // Quiz non trouvé
    }

}
