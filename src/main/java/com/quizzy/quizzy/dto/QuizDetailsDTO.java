package com.quizzy.quizzy.dto;


import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class QuizDetailsDTO {
    private String title;
    private String description;
    private List<QuestionDTO> questions;
}
