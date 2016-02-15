package org.grattis.quiz;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.grattis.quiz.api.*;
import org.grattis.quiz.domain.AnswerRepository;
import org.grattis.quiz.domain.ContestantRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

/**
 * Created by Peter on 2015-12-27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@Slf4j
public class QuizControllerTest {

    @Autowired
    private ContestantRepository contestantRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Value("${local.server.port}")
    int port;

    @Before
    @Transactional
    public void setUp() {
        log.info("Test TCP Port: {}", port);
        RestAssured.port = port;
        answerRepository.deleteAll();
        contestantRepository.deleteAll();
        createQuiz();
    }

    @Test
    public void loginTest() {
        login();
    }

    //
    public QuizRound login() {
        final LoginBean loginBean = new LoginBean();
        loginBean.setEmail("test@test.nu");
        loginBean.setQuiz("Test");

        final Response response =
                given()
                        .body(loginBean)
                        .contentType(ContentType.JSON)
                        .when()
                        .post("/api/login")
                        .then()
                        .statusCode(equalTo(HttpStatus.SC_OK))
                        .body("user.email", equalTo(loginBean.getEmail()))
                        .body("user.id", anything())
                        .body("quiz.name", equalTo("Test"))
                        .body("quiz.quizId", anything())
                        .body("quiz.quizQuestions.size", equalTo(10))
                        .extract()
                        .response();

        final QuizRound quizRound = response.as(QuizRound.class);

        // order test
        IntStream.range(0, quizRound.getQuiz().getQuizQuestions().size())
                .forEach(i -> {
                    assertEquals(i, quizRound.getQuiz().getQuizQuestions().get(i).getSeqno());
                });

        return quizRound;
    }

    @Test
    public void answerTest() {
        final QuizRound quizRound = login();

        final AnswerBean answerBean = new AnswerBean();
        answerBean.setUserId(quizRound.getUser().getId());

        final AtomicInteger score = new AtomicInteger(0);

        quizRound.getQuiz().getQuizQuestions().forEach(quizQuestionBean -> {
            answerBean.setActualAnswer(randomAnswer());
            answerBean.setQuizQuestionId(quizQuestionBean.getQuizQuestionId());
            if (postAnswer(answerBean)) {
                score.incrementAndGet();
            }
        });


        final ResultBean resultBean = getResult(quizRound.getQuiz().getName());
        assertEquals(1, resultBean.getUserResults().size());
        assertEquals(score.get(), resultBean.getUserResults().get(0).getScore());
    }

    @Test
    public void reLoginTest() {
        final QuizRound quizRound = login();

        final AnswerBean answerBean = new AnswerBean();
        answerBean.setUserId(quizRound.getUser().getId());
        answerBean.setActualAnswer(randomAnswer());
        answerBean.setQuizQuestionId(quizRound.getQuiz().getQuizQuestions().get(0).getQuizQuestionId());

        postAnswer(answerBean);
        answerBean.setQuizQuestionId(quizRound.getQuiz().getQuizQuestions().get(1).getQuizQuestionId());
        postAnswer(answerBean);

        final QuizRound newQuizRound = login();

        assertEquals(2, newQuizRound.getUser().getNext());
    }

    @Test
    public void postQuizFromFileTest() {
        final QuizBean quizBean = QuizBeanTest.fromFile("/quiz-test.json");
        quizBean.setName("Another Test");
        postQuiz(quizBean);
    }


    //
    public ResultBean getResult(final String quizName) {
        final Response response =
                when()
                        .get(String.format("/api/quizzes/%s/results", quizName))
                        .then()
                        .statusCode(equalTo(HttpStatus.SC_OK))
                        .extract()
                        .response();

        return response.as(ResultBean.class);
    }

    //
    public boolean postAnswer(final AnswerBean answerBean) {
        final Response response =
                given()
                        .body(answerBean)
                        .contentType(ContentType.JSON)
                        .when()
                        .post("/api/answers")
                        .then()
                        .statusCode(equalTo(HttpStatus.SC_OK))
                        .extract()
                        .response();

        return response.as(StatusBean.class).isStatus();
    }


    @SneakyThrows
    public QuizBean createQuiz() {
        final QuizBean quizBean = new QuizBean();
        quizBean.setName("Test");
        quizBean.setThinkTimeInSeconds(30);
        quizBean.setClosed(false);

        // seqno in reverse order
        IntStream
                .range(0, 10)
                .forEach(i -> quizBean.getQuizQuestions().add(createQuestion("Question-" + i)));

        postQuiz(quizBean);

        return quizBean;
    }


    //
    public QuizBean postQuiz(final QuizBean quizBean) {
        given()
                .body(quizBean)
                .contentType(ContentType.JSON)
                .when()
                .post("/api/quizzes")
                .then()
                .statusCode(equalTo(HttpStatus.SC_OK));
        return quizBean;
    }

    //
    public QuizQuestionBean createQuestion(final String question) {
        final QuizQuestionBean quizQuestionBean = new QuizQuestionBean();
        quizQuestionBean.setQuestion(question);
        quizQuestionBean.setAlternative1("Yes");
        quizQuestionBean.setAlternative2("No");
        quizQuestionBean.setAlternative3("Blue");
        quizQuestionBean.setAlternative4("Green");
        quizQuestionBean.setSeqno(-1);
        quizQuestionBean.setCorrectAnswer(randomAnswer());
        return quizQuestionBean;
    }

    //
    public int randomAnswer() {
        return ThreadLocalRandom.current().nextInt(4);
    }

}
