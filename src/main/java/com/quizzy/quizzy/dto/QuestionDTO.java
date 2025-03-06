package com.quizzy.quizzy.dto;

import java.util.List;

public record QuestionDTO(String id, String title, List<AnswerDTO> answers) {
}
