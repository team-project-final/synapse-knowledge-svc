package com.synapse.knowledge.note.controller;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.global.security.CurrentUser;
import com.synapse.knowledge.global.security.CurrentUserAuth;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.dto.NoteVersionDetailResponse;
import com.synapse.knowledge.note.dto.NoteVersionSummaryResponse;
import com.synapse.knowledge.note.service.NoteService;
import com.synapse.knowledge.note.service.NoteVersionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes/{noteId}/versions")
@RequiredArgsConstructor
public class NoteVersionController {

    private final NoteVersionService noteVersionService;
    private final NoteService noteService;

    @GetMapping
    public ApiResponse<List<NoteVersionSummaryResponse>> listVersions(
        @CurrentUserAuth CurrentUser currentUser,
        @PathVariable Long noteId
    ) {
        return ApiResponse.success(noteVersionService.listVersions(currentUser.userId(), noteId));
    }

    @GetMapping("/{versionNo}")
    public ApiResponse<NoteVersionDetailResponse> getVersion(
        @CurrentUserAuth CurrentUser currentUser,
        @PathVariable Long noteId,
        @PathVariable Integer versionNo
    ) {
        return ApiResponse.success(noteVersionService.getVersion(currentUser.userId(), noteId, versionNo));
    }

    @PostMapping("/{versionNo}/restore")
    public ApiResponse<NoteResponse> restore(
        @CurrentUserAuth CurrentUser currentUser,
        @PathVariable Long noteId,
        @PathVariable Integer versionNo
    ) {
        return ApiResponse.success(noteService.restoreVersion(currentUser.userId(), noteId, versionNo));
    }
}
