package org.grattis.quiz;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
public class WebConfiguration extends RepositoryRestMvcConfiguration {


    @Override
    public RepositoryRestConfiguration config() {
        final RepositoryRestConfiguration config = super.config();
        config.setBasePath("/db-api");
        config.setMaxPageSize(100);
        return config;
    }

}