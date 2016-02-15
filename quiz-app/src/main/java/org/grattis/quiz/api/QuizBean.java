package org.grattis.quiz.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Peter on 2015-12-27.
 */
@Data
public class QuizBean {
    private Long quizId;
    private String name;
    private int thinkTimeInSeconds;
    private boolean closed;
    private List<QuizQuestionBean> quizQuestions = new ArrayList<>();
}
