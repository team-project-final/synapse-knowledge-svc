package com.synapse.knowledge.note.service.support;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WikiLinkParser {
    // ReDoS protection: Limit characters and no nested brackets
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[([^\\]|\\n]{1,200})\\]\\]");

    public Set<String> parse(String content) {
        Set<String> titles = new HashSet<>();
        if (content == null || content.isEmpty()) return titles;

        Matcher matcher = LINK_PATTERN.matcher(content);
        while (matcher.find()) {
            String title = matcher.group(1).trim();
            if (!title.isEmpty()) {
                titles.add(title);
            }
        }
        return titles;
    }
}
