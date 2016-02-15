package org.grattis.quiz.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Peter on 2015-10-30.
 */
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
}
