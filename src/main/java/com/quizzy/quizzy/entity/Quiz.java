package com.quizzy.quizzy.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String ownerUid;
    private String title;
    private String description;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    // ✅ Ajout de l'attribut executionId
    @Column(unique = true, nullable = true)
    private String executionId;

    // ✅ Constructeur par défaut (obligatoire pour JPA)
    public Quiz() {
    }

    // ✅ Nouveau constructeur pour faciliter la création d'un quiz
    public Quiz(String ownerUid, String title, String description) {
        this.ownerUid = ownerUid;
        this.title = title;
        this.description = description;
    }

    // ✅ Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    // ✅ Ajout du getter et setter pour executionId
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
}
