package com.inpt.collaborationplatform.tasks.subtask.mapper;

import com.inpt.collaborationplatform.tasks.subtask.dto.response.SubTaskResponse;
import com.inpt.collaborationplatform.tasks.subtask.entity.SubTask;
import org.springframework.stereotype.Component;

@Component
public class SubTaskMapper {

    public SubTaskResponse toSubTaskResponse(SubTask subTask) {
        return new SubTaskResponse(
                subTask.getId(),
                subTask.getTask().getId(),
                subTask.getAssigneeId(),
                subTask.getTitle(),
                subTask.isDone(),
                subTask.getStatus(),
                subTask.getCreatedAt(),
                subTask.getUpdatedAt()
        );
    }
}
