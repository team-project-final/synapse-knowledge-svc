package com.synapse.knowledge.global.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrometheusActuatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("actuatorPrometheus_비인증요청_shouldReturn200")
    void actuatorPrometheus_비인증요청_shouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("actuatorPrometheus_shouldExposePrometheusTextFormat")
    void actuatorPrometheus_shouldExposePrometheusTextFormat() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/plain"))
            .andExpect(content().string(startsWith("#")))
            .andExpect(content().string(containsString("jvm_")));
    }
}
