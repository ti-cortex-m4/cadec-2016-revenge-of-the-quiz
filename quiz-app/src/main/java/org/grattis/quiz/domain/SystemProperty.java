package org.grattis.quiz.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Peter on 2016-01-20.
 */
@Entity
@Data
public class SystemProperty {

    //
    public static SystemProperty EMPTY = new SystemProperty();

    @Id
    private String name;

    @Column
    private String value;
}
