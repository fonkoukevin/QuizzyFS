package com.quizzy.quizzy.dto;

import java.util.List;

public record QuizDetailsDTO(String title, String description, List<QuestionDTO> questions) {
}
