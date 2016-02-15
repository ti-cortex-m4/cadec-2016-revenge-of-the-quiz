package org.grattis.quiz;

import com.mysema.query.types.expr.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.grattis.quiz.api.*;
import org.grattis.quiz.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

/**
 * Created by Peter on 2015-12-17.
 */
@Service
@Slf4j
public class QuizService {

    static final int UNKNOWN = -1;

    @Autowired
    private ContestantRepository contestantRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private SystemPropertyRepository systemPropertyRepository;

    private DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    public static String LOCK_DB_NAME = "LockDB";
    //
    @Transactional
    public QuizRound login(final LoginBean login) {
        final QuizRound quizRound = new QuizRound();

        final UserBean userBean = addUser(login.getEmail());

        quizRound.setUser(userBean);
        quizRound.setQuiz(getQuiz(login.getQuiz()));
        quizRound.setClosed(quizRound.getQuiz().isClosed());

        if (userBean.getNext() == UNKNOWN) {
            prepareForNext(quizRound);
        }

        return quizRound;
    }


    @Transactional(readOnly = true)
    public SystemProperty getSystemProperty(final String name) {
        final SystemProperty systemProperty = systemPropertyRepository.findOne(name);
        return (systemProperty == null) ? SystemProperty.EMPTY : systemProperty;
    }

    @Transactional
    public boolean isLocked() {
        return String.valueOf(true).equals(getSystemProperty(LOCK_DB_NAME).getValue());
    }

    @Transactional
    public void lock() {
        SystemProperty systemProperty = getSystemProperty(LOCK_DB_NAME);
        if (systemProperty == SystemProperty.EMPTY) {
            systemProperty = new SystemProperty();
            systemProperty.setName(LOCK_DB_NAME);
            systemProperty.setValue(String.valueOf(true));
        } else {
            systemProperty.setValue(String.valueOf(true));
        }
        systemPropertyRepository.save(systemProperty);
    }

    @Transactional
    public void unlock() {
        SystemProperty systemProperty = getSystemProperty(LOCK_DB_NAME);
        if (systemProperty != null) {
            systemProperty.setValue(String.valueOf(false));
            systemPropertyRepository.save(systemProperty);
        }
    }

    //
    public void prepareForNext(final QuizRound quizRound) {
        final QAnswer a = QAnswer.answer;
        final BooleanExpression predicate =
                a.contestant.id.eq(
                        quizRound.getUser().getId()
                ).and(
                        a.quizQuestion.quiz.quizId.eq(
                                quizRound.getQuiz().getQuizId()
                        )
                );

        final AtomicReference<Answer> lastAnswerRef = new AtomicReference<>(null);
        final ArrayList<Boolean> scores = new ArrayList<>();
        StreamSupport.stream(answerRepository.findAll(predicate, a.quizQuestion.seqno.asc()).spliterator(), false)
                .forEach(answer -> {
                    lastAnswerRef.set(answer);
                    scores.add(answer.isScore());
                });

        final Answer lastAnswer = lastAnswerRef.get();
        if (lastAnswer != null) {
            quizRound.getUser().setNext(lastAnswer.getQuizQuestion().getSeqno() + 1);
            quizRound.getUser().setScores(scores);
        } else {
            quizRound.getUser().setNext(0);
        }

        log.debug("Already active user {}", quizRound.getUser());
    }

    //
    @Transactional
    public void addQuiz(final QuizBean quizBean) {
        final Quiz oldQuiz = quizRepository.findByName(quizBean.getName());
        if (oldQuiz != null) {
            quizRepository.delete(oldQuiz.getQuizId());
            quizRepository.flush();
        }
        final Quiz quiz = dozerBeanMapper.map(quizBean, Quiz.class);
        final AtomicInteger n = new AtomicInteger(0);

        quiz.getQuizQuestions().forEach(q -> {
            q.setQuiz(quiz);
            q.setSeqno(n.getAndIncrement());
        });

        quizRepository.save(quiz);
    }

    //
    @Transactional(readOnly = true)
    public ResultBean getResults(final String quizName) {

        final HashMap<Long, UserResultBean> map = new HashMap<>();

        final BooleanExpression predicate = QAnswer.answer.quizQuestion.quiz.name.eq(quizName);

        final ResultBean newResultBean = new ResultBean();
        final AtomicBoolean closedFlag = new AtomicBoolean(false);

        StreamSupport.stream(answerRepository.findAll(predicate).spliterator(), false)
                .forEach(answer -> {
                    UserResultBean urb = map.get(answer.getContestant().getId());
                    if (urb == null) {
                        urb = new UserResultBean();
                        urb.setUser(dozerBeanMapper.map(answer.getContestant(), UserBean.class));
                        map.put(answer.getContestant().getId(), urb);
                        newResultBean.getUserResults().add(urb);
                    }
                    urb.incAnswers();
                    if (answer.isScore()) {
                        urb.incScore();
                    }
                    urb.incElapsed(answer.getElapsed());
                    urb.setTimestamp(answer.getRegistered().getTime());
                    if (!closedFlag.get()) {
                        newResultBean.setClosed(answer.getQuizQuestion().getQuiz().isClosed());
                        closedFlag.set(true);
                    }
                });

        Collections.sort(newResultBean.getUserResults());

        return newResultBean;
    }

    //
    void disableQuiz(final String name, final boolean closed) {
        final Quiz quiz = quizRepository.findByName(name);
        quiz.setClosed(closed);
        quizRepository.save(quiz);
    }

    @Transactional
    public void closeQuiz(final String name) {
        log.info("Closing Quiz {}", name);
        disableQuiz(name, true);
    }

    @Transactional
    public void openQuiz(final String name) {
        log.info("Open Quiz {}", name);
        disableQuiz(name, false);
    }

    //
    @Transactional
    public boolean addAnswer(final AnswerBean answerBean) {
        final QuizQuestion quizQuestion = quizQuestionRepository.getOne(answerBean.getQuizQuestionId());
        if (quizQuestion.getQuiz().isClosed()) {
            throw new IllegalArgumentException("Quiz " + quizQuestion.getQuiz().getName() + " has been closed");
        }
        final Contestant contestant = contestantRepository.getOne(answerBean.getUserId());
        final Answer answer = new Answer();
        answer.setContestant(contestant);
        answer.setQuizQuestion(quizQuestion);
        answer.setActualAnswer(answerBean.getActualAnswer());
        answer.setElapsed(answerBean.getElapsed());
        answerRepository.save(answer);
        log.debug("actualAnswer {}, correctAnswer {}", answer.getActualAnswer(), quizQuestion.getCorrectAnswer());
        return (answer.getActualAnswer() == quizQuestion.getCorrectAnswer());
    }

    //
    public UserBean addUser(final String email) {
        Contestant contestant = contestantRepository.findByEmail(email);
        int next = UNKNOWN;
        if (contestant == null) {
            contestant = new Contestant();
            contestant.setEmail(email);
            contestant = contestantRepository.save(contestant);
            next = 0;
        }
        final UserBean userBean = dozerBeanMapper.map(contestant, UserBean.class);
        userBean.setNext(next);
        return userBean;
    }

    // hide all correct answers when retrieving a quiz
    public QuizBean getQuiz(final String name) {
        final Quiz quiz = quizRepository.findByName(name);
        if (quiz == null) {
            throw new IllegalArgumentException("No such quiz: " + name);
        }
        return dozerBeanMapper.map(quiz, QuizBean.class);
    }

    //
    @Transactional
    public void clearAll() {
        answerRepository.deleteAll();
        contestantRepository.deleteAll();
        quizRepository.deleteAll();
    }
}
