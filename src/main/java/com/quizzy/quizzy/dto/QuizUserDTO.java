package com.quizzy.quizzy.dto;

import java.util.Map;

public record QuizUserDTO(String id, String title, String description, Map<String, String> _links) {
}
