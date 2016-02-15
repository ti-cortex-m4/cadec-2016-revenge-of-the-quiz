package org.grattis.quiz.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by Peter on 2015-12-28.
 */
@Data
@AllArgsConstructor(staticName = "of")
public class StatusBean {
    private boolean status;

    public StatusBean() {
    }
}

