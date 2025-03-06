package com.quizzy.quizzy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    private String uid;
    private String username;

    // ✅ Constructeur par défaut requis par JPA
    public User() {}

    // ✅ Constructeur avec uid et username
    public User(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    // ✅ Getters et Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
