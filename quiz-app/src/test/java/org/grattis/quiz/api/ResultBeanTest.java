package org.grattis.quiz.api;

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Peter on 2015-12-28.
 */
public class ResultBeanTest {

    @Test
    @SneakyThrows
    public void orderTest() {
        final long start = System.currentTimeMillis();
        ResultBean resultBean = new ResultBean();

        IntStream.range(0, 10)
                .forEach(i -> resultBean.getUserResults().add(createUserResultBean(String.valueOf(i+1), i+1, i)));
        IntStream.range(0, 10)
                .forEach(i -> resultBean.getUserResults().add(createUserResultBean(String.valueOf(i+1), i+1, i+1)));

        Collections.sort(resultBean.getUserResults());

        assertEquals(10, resultBean.getUserResults().get(0).getScore());
        assertEquals(10, resultBean.getUserResults().get(1).getScore());
        assertEquals(9, resultBean.getUserResults().get(2).getScore());
        assertEquals(9, resultBean.getUserResults().get(3).getScore());


        assertTrue(resultBean.getUserResults().get(0).getElapsed() < resultBean.getUserResults().get(1).getElapsed());
        assertTrue(resultBean.getUserResults().get(2).getElapsed() < resultBean.getUserResults().get(3).getElapsed());
    }

    //
    UserResultBean createUserResultBean(final String email, final int score, final int elapsed) {
        final UserResultBean userResultBean = new UserResultBean();
        userResultBean.setAnswers(10);
        userResultBean.setScore(score);
        userResultBean.setElapsed(elapsed);
        userResultBean.setStart(System.currentTimeMillis());
        userResultBean.setEnd(System.currentTimeMillis());
        final UserBean userBean = new UserBean();
        userBean.setEmail(email);
        userBean.setId(-1L);
        userResultBean.setUser(userBean);
        return userResultBean;
    }
}
