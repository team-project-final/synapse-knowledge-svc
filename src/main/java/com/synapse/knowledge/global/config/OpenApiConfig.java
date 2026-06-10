package com.synapse.knowledge.global.config;

import com.synapse.knowledge.global.security.CurrentUser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(CurrentUser.class);
    }

    @Bean
    OpenAPI knowledgeOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Synapse Knowledge API")
                .version("v1"));
    }
}
