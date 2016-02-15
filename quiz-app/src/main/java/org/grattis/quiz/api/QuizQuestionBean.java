package org.grattis.quiz.api;


import lombok.Data;
import lombok.ToString;

/**
 * Created by Peter on 2015-12-27.
 */
@Data
@ToString(exclude = "correctAnswer")
public class QuizQuestionBean {
    private Long quizQuestionId;
    private int seqno;
    private String question;
    private String alternative1;
    private String alternative2;
    private String alternative3;
    private String alternative4;
    private int correctAnswer;
}
