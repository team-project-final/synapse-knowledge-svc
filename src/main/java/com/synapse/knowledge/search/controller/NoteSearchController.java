package com.synapse.knowledge.search.controller;

import com.synapse.knowledge.search.service.SearchService;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.shared.CurrentUser;
import com.synapse.knowledge.shared.CurrentUserAuth;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/notes")
public class NoteSearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public SearchPageResponse search(
        @CurrentUserAuth CurrentUser currentUser,
        @RequestParam("q") @NotBlank String query,
        @RequestParam(value = "cursor", required = false) String cursor,
        @RequestParam(value = "limit", defaultValue = "20") @Min(1) @Max(100) int limit,
        @RequestParam(value = "tags", required = false) List<@Size(max = 30) String> tags
    ) {
        return searchService.search(currentUser.userId(), new SearchRequest(query, cursor, limit, tags));
    }
}
