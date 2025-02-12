package com.quizzy.quizzy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Generates a unique ID
    private String id;

    private String title;

    @Column(nullable = false)
    private String ownerUid; // 🔥 UID of the user who created the quiz
}
