package org.grattis.quiz.api;

import lombok.Data;

import java.util.Date;

/**
 * Created by Peter on 2015-12-28.
 */
@Data
public class UserResultBean implements Comparable<UserResultBean> {
    private UserBean user;
    private int answers;
    private int score;
    private int elapsed;
    private long start = Long.MAX_VALUE;
    private long end = Long.MIN_VALUE;

    //
    public void incAnswers() {
        this.answers++;
    }

    //
    public void incElapsed(int elapsed) {
        this.elapsed = (this.elapsed + elapsed);
    }

    //
    public void incScore() {
        this.score++;
    }

    //
    public void setTimestamp(final Long timestamp) {
        setStart(Math.min(getStart(), timestamp));
        setEnd(Math.max(getEnd(), timestamp));
    }

    //
    public long elapsed() {
        return (this.end - this.start);
    }

    @Override
    public int compareTo(final UserResultBean b) {
        // 1. descending by score
        final int r = Integer.compare(b.getScore(), getScore());
        // 2. ascending by elapsed
        return (r != 0) ? r : Long.compare(getElapsed(), b.getElapsed());
    }
}
