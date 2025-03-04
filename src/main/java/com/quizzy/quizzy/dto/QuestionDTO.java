package com.quizzy.quizzy.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDTO {
    private String text; // Utilise "text" pour correspondre à l'entité Question
    private List<AnswerDTO> answers;
}
