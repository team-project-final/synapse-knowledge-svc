package com.synapse.knowledge.shared;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

@Component
public class MarkdownSanitizer {
    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.LINKS)
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.TABLES);

    public String sanitize(String rawMarkdown) {
        return rawMarkdown == null ? null : POLICY.sanitize(rawMarkdown);
    }
}
