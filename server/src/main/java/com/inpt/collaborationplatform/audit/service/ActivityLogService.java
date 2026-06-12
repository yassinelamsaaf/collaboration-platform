package com.inpt.collaborationplatform.audit.service;

import com.inpt.collaborationplatform.Identity.service.IdentityAccessService;
import com.inpt.collaborationplatform.audit.dto.response.ActivityLogResponse;
import com.inpt.collaborationplatform.audit.entity.ActivityLog;
import com.inpt.collaborationplatform.audit.mapper.ActivityLogMapper;
import com.inpt.collaborationplatform.audit.repository.ActivityLogRepository;
import com.inpt.collaborationplatform.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    private final IdentityAccessService identityAccessService;

    @Transactional
    public void log(String actorId, String projectId, String entityType, String entityId, String action, String details) {
        ActivityLog saved = activityLogRepository.save(ActivityLog.builder()
                .actorId(actorId)
                .projectId(projectId)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .build());
        log.info("ActivityLogService.log: saved id={}, action={}, entityId={}", saved.getId(), saved.getAction(), saved.getEntityId());
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> listByProject(String projectId, Pageable pageable) {
        var page = activityLogRepository.findByProjectIdOrderByTimestampDesc(projectId, pageable);
        return new PageResponse<>(
                page.getContent().stream().map(log -> {
                    String name = identityAccessService.requireUserUsername(log.getActorId());
                    return activityLogMapper.toResponse(log, name);
                }).toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
