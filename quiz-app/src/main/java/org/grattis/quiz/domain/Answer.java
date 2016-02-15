package org.grattis.quiz.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Peter on 2015-10-30.
 */
@Entity
@Data
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date registered;

    @ManyToOne
    @JoinColumn(name = "contestant_id", foreignKey = @ForeignKey(name = "answer_contestant_fk"))
    private Contestant contestant;

    @ManyToOne
    @JoinColumn(name = "quizquestion_id", foreignKey = @ForeignKey(name = "answer_quizquestion_fk"))
    private QuizQuestion quizQuestion;

    @Column
    private int actualAnswer;

    @Column
    private int elapsed;

    //
    public boolean isScore() {
        return (getActualAnswer() == getQuizQuestion().getCorrectAnswer());
    }

    @PrePersist
    public void prePersist() {
        setRegistered(new Date());
    }
}
