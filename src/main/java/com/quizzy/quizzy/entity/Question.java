package com.quizzy.quizzy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identifiant unique de la question

    private String text; // Le texte de la question

    @ElementCollection
    private List<String> choices; // Les choix de réponse (si tu veux les stocker sous forme de liste de chaînes)

    private String correctAnswer; // La bonne réponse

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz; // Le quiz auquel cette question appartient

}
