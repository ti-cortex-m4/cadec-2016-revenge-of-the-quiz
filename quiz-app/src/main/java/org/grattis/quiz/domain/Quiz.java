package org.grattis.quiz.domain;

import lombok.Data;
import org.grattis.quiz.api.QuizBean;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Peter on 2015-10-30.
 */
@Entity
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @Column(unique = true)
    private String name;

    @Column
    private int thinkTimeInSeconds;

    @Column
    private boolean closed = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    @PostLoad
    public void sortQuestions() {
        Collections.sort(getQuizQuestions());
    }
}
