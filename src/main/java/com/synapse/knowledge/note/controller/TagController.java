package com.synapse.knowledge.note.controller;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.global.security.CurrentUser;
import com.synapse.knowledge.global.security.CurrentUserAuth;
import com.synapse.knowledge.note.dto.PopularTagResponse;
import com.synapse.knowledge.note.dto.TagAutoCompleteResponse;
import com.synapse.knowledge.note.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/autocomplete")
    public ApiResponse<List<TagAutoCompleteResponse>> autocomplete(
        @CurrentUserAuth CurrentUser currentUser,
        @RequestParam String q
    ) {
        return ApiResponse.success(tagService.autocomplete(currentUser.userId(), q));
    }

    @GetMapping("/popular")
    public ApiResponse<List<PopularTagResponse>> popular(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.success(tagService.getPopular(limit));
    }
}
