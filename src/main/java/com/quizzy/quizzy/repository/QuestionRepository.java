package com.quizzy.quizzy.repository;

import com.quizzy.quizzy.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizId(String quiz_id);
}

