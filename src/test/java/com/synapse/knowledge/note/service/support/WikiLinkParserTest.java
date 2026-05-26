package com.synapse.knowledge.note.service.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class WikiLinkParserTest {

    private final WikiLinkParser parser = new WikiLinkParser();

    @Test
    @DisplayName("parse_기본패턴_shouldExtractTitles")
    void parse_기본패턴_shouldExtractTitles() {
        String content = "Hello [[Java]] and [[Spring Boot]]";
        Set<String> titles = parser.parse(content);
        assertThat(titles).containsExactlyInAnyOrder("Java", "Spring Boot");
    }

    @Test
    @DisplayName("parse_중복제목_shouldReturnUniqueSet")
    void parse_중복제목_shouldReturnUniqueSet() {
        String content = "[[Java]] and [[Java]]";
        Set<String> titles = parser.parse(content);
        assertThat(titles).hasSize(1).contains("Java");
    }

    @Test
    @DisplayName("parse_공백포함_shouldTrimTitles")
    void parse_공백포함_shouldTrimTitles() {
        String content = "[[ Java  ]]";
        Set<String> titles = parser.parse(content);
        assertThat(titles).contains("Java");
    }

    @Test
    @DisplayName("parse_비정상패턴_shouldIgnore")
    void parse_비정상패턴_shouldIgnore() {
        String content = "[[No closing";
        Set<String> titles = parser.parse(content);
        assertThat(titles).isEmpty();
    }
}
