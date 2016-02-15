package org.grattis.quiz.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Peter on 2016-01-20.
 */
public interface SystemPropertyRepository extends JpaRepository<SystemProperty, String> {
}
