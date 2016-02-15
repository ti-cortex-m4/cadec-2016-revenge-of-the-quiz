package org.grattis.quiz.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.grattis.quiz.domain.Quiz;
import org.junit.Test;
import org.omg.CORBA.Object;

import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Peter on 2015-12-28.
 */
public class QuizBeanTest {

    static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows
    public void deSerializeTest() {
        fromFile("/quiz-test.json");
    }

    @SneakyThrows
    static byte[] fileContent(final String contentFile) {
        @Cleanup
        final InputStream in = contentFile.getClass().getResourceAsStream(contentFile);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toByteArray();
    }

    @SneakyThrows
    public static QuizBean fromFile(final String jsonFile) {
        final byte[] source = fileContent(jsonFile);
        final QuizBean quizBean = objectMapper.readValue(source, QuizBean.class);
        return quizBean;
    }
}
