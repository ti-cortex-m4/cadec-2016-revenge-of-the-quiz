package org.grattis.quiz.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 2015-12-28.
 */
@Data
public class ResultBean {
    private long timstamp = System.currentTimeMillis();

    private List<UserResultBean> userResults = new ArrayList<>();

    private boolean closed;

    //
    public boolean expired(long resultTTL) {
        return (System.currentTimeMillis() - timstamp) > resultTTL;
    }
}


