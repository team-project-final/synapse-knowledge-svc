package com.synapse.knowledge.search.controller;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.search.dto.SearchComparisonReport;
import com.synapse.knowledge.search.service.SearchAccuracyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/search")
public class SearchAccuracyAdminController {

    private final SearchAccuracyService searchAccuracyService;

    @PostMapping("/accuracy-test")
    public ApiResponse<SearchComparisonReport> runAccuracyTest() {
        return ApiResponse.success(searchAccuracyService.runAccuracyTest());
    }

    @GetMapping("/accuracy-report")
    public ApiResponse<SearchComparisonReport> getAccuracyReport() {
        return ApiResponse.success(searchAccuracyService.getLatestReport());
    }
}
