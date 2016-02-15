package org.grattis.quiz.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Peter on 2015-10-30.
 */
@Entity
@Data
public class Contestant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date registered;


    @Column(unique = true)
    private String email;

    @PrePersist
    public void prePersist() {
        setRegistered(new Date());
    }
}
