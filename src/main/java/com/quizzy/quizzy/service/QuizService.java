package com.quizzy.quizzy.service;

import com.quizzy.quizzy.entity.Quiz;
import com.quizzy.quizzy.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public List<Quiz> getQuizzesByUser(String ownerUid) {
        return quizRepository.findByOwnerUid(ownerUid); // 🔥 Retrieve user-specific quizzes
    }

    public Quiz createQuiz(String ownerUid, String title, String description) {
        Quiz quiz = new Quiz();
        quiz.setOwnerUid(ownerUid);
        quiz.setTitle(title);
        quiz.setDescription(description);
        return quizRepository.save(quiz);
    }
}
