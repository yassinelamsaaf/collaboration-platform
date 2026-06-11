package com.inpt.collaborationplatform.tasks.timeentry.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeEntryResponse(
        String id,
        String taskId,
        String userId,
        int durationMinutes,
        LocalDate date,
        String description,
        LocalDateTime createdAt
) {
}
