package com.inpt.collaborationplatform.audit.mapper;

import com.inpt.collaborationplatform.audit.dto.response.ActivityLogResponse;
import com.inpt.collaborationplatform.audit.entity.ActivityLog;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLogResponse toResponse(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getActorId(),
                log.getProjectId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getDetails(),
                log.getTimestamp()
        );
    }
}
