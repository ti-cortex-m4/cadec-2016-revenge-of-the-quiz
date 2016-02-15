package org.grattis.quiz.api;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 2015-12-27.
 */
@Data
public class UserBean {
    private Long id;
    private String email;
    private int next;
    private List<Boolean> scores = new ArrayList<>();
}
