package com.quizzy.quizzy.service;

import com.quizzy.quizzy.entity.Quiz;
import com.quizzy.quizzy.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    // ✅ Retrieve all quizzes for a user based on their UID
    public List<Quiz> getQuizzesByUser(String ownerUid) {
        return quizRepository.findByOwnerUid(ownerUid); // 🔥 Retrieve user-specific quizzes
    }

    // ✅ Create a new quiz
    public Quiz createQuiz(String ownerUid, String title, String description) {
        Quiz quiz = new Quiz();
        quiz.setOwnerUid(ownerUid);
        quiz.setTitle(title);
        quiz.setDescription(description);
        return quizRepository.save(quiz); // ✅ Save quiz to the database
    }

    // ✅ Retrieve a quiz by its ID and ensure that the user is the owner
    public Quiz getQuizByIdAndOwner(String id, String ownerUid) {
        // Find the quiz by its ID
        Optional<Quiz> optionalQuiz = quizRepository.findById(id);

        // If the quiz exists and the owner UID matches, return the quiz
        if (optionalQuiz.isPresent() && optionalQuiz.get().getOwnerUid().equals(ownerUid)) {
            return optionalQuiz.get();
        }

        // If quiz does not exist or owner UID doesn't match, return null
        return null;
    }
}
