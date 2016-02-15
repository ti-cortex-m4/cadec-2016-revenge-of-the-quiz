package org.grattis.quiz.api;

import lombok.Data;

/**
 * Created by Peter on 2015-12-27.
 */
@Data
public class QuizRound {
    private UserBean user;
    private QuizBean quiz;
    private boolean closed;
}
