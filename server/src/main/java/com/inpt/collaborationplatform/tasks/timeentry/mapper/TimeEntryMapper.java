package com.inpt.collaborationplatform.tasks.timeentry.mapper;

import com.inpt.collaborationplatform.tasks.timeentry.dto.response.TimeEntryResponse;
import com.inpt.collaborationplatform.tasks.timeentry.entity.TimeEntry;
import org.springframework.stereotype.Component;

@Component
public class TimeEntryMapper {

    public TimeEntryResponse toTimeEntryResponse(TimeEntry entry) {
        return new TimeEntryResponse(
                entry.getId(),
                entry.getTask().getId(),
                entry.getUserId(),
                entry.getDurationMinutes(),
                entry.getDate(),
                entry.getDescription(),
                entry.getCreatedAt()
        );
    }
}
