package org.grattis.quiz.domain;

import com.mysema.query.types.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


/**
 * Created by Peter on 2015-10-30.
 */
public interface AnswerRepository extends JpaRepository<Answer, Long>, QueryDslPredicateExecutor<Answer> {
}
