package com.quizzy.quizzy.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDTO {
    private String title;
    private List<AnswerDTO> answers;
}
