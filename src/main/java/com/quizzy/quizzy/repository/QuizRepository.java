package com.quizzy.quizzy.repository;



import com.quizzy.quizzy.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, String> {
    List<Quiz> findByOwnerUid(String ownerUid); // 🔥 Find quizzes for a user
}
