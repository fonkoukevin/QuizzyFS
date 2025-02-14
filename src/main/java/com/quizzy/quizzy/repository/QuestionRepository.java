package com.quizzy.quizzy.repository;

import com.quizzy.quizzy.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}

