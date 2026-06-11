package com.inpt.collaborationplatform.tasks.timeentry.controller;

import com.inpt.collaborationplatform.shared.dto.MessageResponse;
import com.inpt.collaborationplatform.shared.util.SecurityUtils;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import com.inpt.collaborationplatform.tasks.timeentry.dto.request.CreateTimeEntryRequest;
import com.inpt.collaborationplatform.tasks.timeentry.dto.response.TimeEntryResponse;
import com.inpt.collaborationplatform.tasks.timeentry.service.TimeEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectRef}/teams/{teamRef}/tasks/{taskId}/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    @PostMapping
    public ResponseEntity<TimeEntryResponse> logTime(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @Valid @RequestBody CreateTimeEntryRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeEntryService.logTime(projectRef, teamRef, taskId, request, SecurityUtils.currentUserId(authentication)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TimeEntryResponse>> listTimeEntries(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(timeEntryService.listTimeEntries(projectRef, teamRef, taskId, SecurityUtils.currentUserId(authentication), pageable));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<MessageResponse> deleteTimeEntry(
            @PathVariable String projectRef,
            @PathVariable String teamRef,
            @PathVariable String taskId,
            @PathVariable String entryId,
            Authentication authentication
    ) {
        timeEntryService.deleteTimeEntry(projectRef, teamRef, taskId, entryId, SecurityUtils.currentUserId(authentication));
        return ResponseEntity.ok(new MessageResponse("Time entry deleted successfully"));
    }

}
