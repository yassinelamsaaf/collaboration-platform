package com.inpt.collaborationplatform.audit.controller;

import com.inpt.collaborationplatform.audit.dto.response.ActivityLogResponse;
import com.inpt.collaborationplatform.shared.util.SecurityUtils;
import com.inpt.collaborationplatform.audit.service.ActivityLogService;
import com.inpt.collaborationplatform.projects.project.service.ProjectAccessService;
import com.inpt.collaborationplatform.projects.project.service.ProjectLookupService;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectRef}/activity-log")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final ProjectLookupService projectLookupService;
    private final ProjectAccessService projectAccessService;

    @GetMapping
    public ResponseEntity<PageResponse<ActivityLogResponse>> listActivityLog(
            @PathVariable String projectRef,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var project = projectLookupService.requireProject(projectRef);
        projectAccessService.requireViewer(project, SecurityUtils.currentUserId(authentication));

        return ResponseEntity.ok(
                activityLogService.listByProject(project.getId(), pageable));
    }

}
