package com.quizzy.quizzy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title; // 🔥 Le texte de la réponse

    @Column(nullable = false)
    private boolean isCorrect; // ✅ Si la réponse est correcte ou non

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // 🔥 Relation avec la question
}
