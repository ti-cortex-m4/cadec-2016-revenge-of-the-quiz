package org.grattis.quiz;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.server.web.WebServlet;
import org.h2.tools.Server;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.servlet.ServletContext;

/**
 * Creates services to be used in development mode only.
 *
 * @author peter
 */
@Configuration
@Profile("devtools")
@Slf4j
public class DevConfiguration {
    /**
     * Activates H2 embedded dev database console.
     *
     * @return the registration bean.
     */
    @Bean
    public ServletRegistrationBean h2servletRegistration() {
        return new ServletRegistrationBean(new WebServlet(), "/db-console/*");
    }

    @Bean(initMethod="start", destroyMethod="stop")
    @SneakyThrows
    public Server initH2TCPServer(final ServletContext servletContext) {
        log.debug("Initializing H2 TCP Server");
        return Server.createTcpServer( "-tcp", "-tcpAllowOthers", "-tcpPort", "9092" );
    }
}

