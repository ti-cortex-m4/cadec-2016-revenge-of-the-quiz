package org.grattis.quiz;

import lombok.extern.slf4j.Slf4j;
import org.grattis.quiz.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter on 2015-10-20.
 */
@RestController
@Slf4j
@RequestMapping("/api")
public class QuizController {

    @Value("${quiz.result.ttl}")
    private long resultTTL;

    @Autowired
    private QuizService quizService;

    private Map<String, ResultBean> resultMap = Collections.synchronizedMap(new HashMap<>());

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<QuizRound> login(final @RequestBody LoginBean login) {
        log.info("POST Login is {}", login);
        final QuizRound quizRound = quizService.login(login);
        log.info("OK Quiz is {}", quizRound);
        return ResponseEntity.ok(quizRound);
    }

    @RequestMapping(value = "/quizzes", method = RequestMethod.POST)
    public ResponseEntity<StatusBean> quizzes(final @RequestBody QuizBean quizBean) {
        log.info("POST Quiz is {}", quizBean);
        if (quizService.isLocked()) {
            throw new IllegalStateException("Locked");
        }
        quizService.addQuiz(quizBean);
        log.info("OK");
        return ResponseEntity.ok(StatusBean.of(true));
    }

    @RequestMapping(value = "/quizzes/{quizName}/results", method = RequestMethod.GET)
    public ResponseEntity<ResultBean> result(final @PathVariable String quizName) {
        log.info("GET Results for {}", quizName);
        ResultBean resultBean = resultMap.get(quizName);
        if (resultBean == null || resultBean.expired(resultTTL)) {
            synchronized (resultMap) {
                resultBean = resultMap.get(quizName);
                if (resultBean == null || resultBean.expired(resultTTL)) {
                    log.debug("Refresh ResultBean");
                    resultBean = quizService.getResults(quizName);
                    resultMap.put(quizName, resultBean);
                }
            }
        }
        log.info("OK Answer is {}", resultBean);
        return ResponseEntity.ok(resultBean);
    }


    @RequestMapping(value = "/answers", method = RequestMethod.POST)
    public ResponseEntity<StatusBean> answer(final @RequestBody AnswerBean answerBean) {
        log.info("POST Answer is {}", answerBean);
        final boolean correct = quizService.addAnswer(answerBean);
        log.info("OK Answer is {}", correct);
        return ResponseEntity.ok(StatusBean.of(correct));
    }

    @RequestMapping(value = "/clearall", method = RequestMethod.GET)
    public ResponseEntity<StatusBean> clear() {
        log.info("GET CLear all");
        if (quizService.isLocked()) {
            throw new IllegalStateException("Locked");
        }
        synchronized (resultMap) {
            resultMap.clear();
        }
        quizService.clearAll();
        log.info("OK");
        return ResponseEntity.ok(StatusBean.of(true));
    }

    @RequestMapping(value = "/locks/{name}", method = RequestMethod.PUT)
    public ResponseEntity<StatusBean> lock(final @PathVariable String name) {
        log.info("PUT Lock {}, name");
        boolean locked = false;
        if (QuizService.LOCK_DB_NAME.equals(name)) {
            quizService.lock();
            locked = true;
        }
        return ResponseEntity.ok(StatusBean.of(locked));
    }

    @RequestMapping(value = "/locks/{name}", method = RequestMethod.DELETE)
    public ResponseEntity<StatusBean> unlock(final @PathVariable String name) {
        log.info("DELETE Lock {}", name);
        boolean unlocked = false;
        if (QuizService.LOCK_DB_NAME.equals(name)) {
            quizService.unlock();
            unlocked = true;
        }
        return ResponseEntity.ok(StatusBean.of(unlocked));
    }


    @RequestMapping(value = "/quizzes/{name}/close", method = RequestMethod.DELETE)
    public ResponseEntity<StatusBean> openQuiz(final @PathVariable String name) {
        log.info("DELETE Closed {}", name);
        if (quizService.isLocked()) {
            throw new IllegalStateException("Locked");
        }
        quizService.openQuiz(name);
        return ResponseEntity.ok(StatusBean.of(true));
    }


    @RequestMapping(value = "/quizzes/{name}/close", method = RequestMethod.PUT)
    public ResponseEntity<StatusBean> closeQuiz(final @PathVariable String name) {
        log.info("PUT Closes {}", name);
        quizService.closeQuiz(name);
        return ResponseEntity.ok(StatusBean.of(true));
    }
}
