package com.quizzy.quizzy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String ownerUid;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'Default Quiz Title'")
    private String title = "Default Quiz Title";

    @Column(nullable = false, columnDefinition = "TEXT DEFAULT 'Default Quiz Description'")
    private String description = "Default Quiz Description";

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions; // Liste des questions associées à ce quiz

}






