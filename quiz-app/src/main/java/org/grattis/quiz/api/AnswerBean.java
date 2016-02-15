package org.grattis.quiz.api;

import lombok.Data;
import lombok.ToString;

/**
 * Created by Peter on 2015-12-27.
 */
@Data
//@ToString(exclude = "actualAnswer")
public class AnswerBean {
    private Long userId;
    private Long quizQuestionId;
    private int actualAnswer;
    private int elapsed;
}
