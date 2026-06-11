package com.synapse.knowledge.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.synapse.knowledge.global.security.CurrentUserArgumentResolver;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

class WebMvcConfigTest {

    @Test
    @DisplayName("addArgumentResolvers_shouldCurrentUserResolver등록")
    void addArgumentResolvers_shouldCurrentUserResolver등록() {
        CurrentUserArgumentResolver resolver = mock(CurrentUserArgumentResolver.class);
        WebMvcConfig webMvcConfig = new WebMvcConfig(resolver);
        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        webMvcConfig.addArgumentResolvers(resolvers);

        assertThat(resolvers).containsExactly(resolver);
    }
}
