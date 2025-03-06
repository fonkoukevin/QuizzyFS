package com.quizzy.quizzy.dto;

import java.util.List;
import java.util.Map;

public record AllQuizUserDTO(List<QuizUserDTO> data, Map<String, String> _links) {
}
