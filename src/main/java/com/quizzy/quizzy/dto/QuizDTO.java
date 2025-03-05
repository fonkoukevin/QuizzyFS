package com.quizzy.quizzy.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class QuizDTO {
    private String id;
    private String title;
    private String description;
}
