package org.grattis.quiz.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Peter on 2015-10-30.
 */
@Entity
@Data
public class QuizQuestion implements Comparable<QuizQuestion> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizQuestionId;

    @ManyToOne
    @JoinColumn(name = "quiz_id", foreignKey = @ForeignKey(name = "quizquestion_quiz_fk"))
    private Quiz quiz;

    @Column
    private int seqno;

    @Column
    private String question;

    @Column
    private String alternative1;

    @Column
    private String alternative2;

    @Column
    private String alternative3;

    @Column
    private String alternative4;

    @Column
    private int correctAnswer;

    @Override
    public int compareTo(final QuizQuestion b) {
        return (getSeqno() - b.getSeqno());
    }
}
