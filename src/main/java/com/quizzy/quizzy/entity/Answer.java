package com.quizzy.quizzy.entity;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE") // 🔥 Définit une valeur par défaut pour MySQL
    private boolean correct = false;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    // ✅ Constructeur avec paramètres
// ✅ Constructeur avec texte, état correct et question associée
    public Answer(String text, boolean correct, Question question) {
        this.text = text;
        this.correct = correct;
        this.question = question;
    }


    // ✅ Constructeur vide (pour Hibernate)
    public Answer() {
        this.correct = false; // 🔥 Assure que la valeur ne soit jamais null
    }
}
