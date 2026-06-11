package com.inpt.collaborationplatform.tasks.label.mapper;

import com.inpt.collaborationplatform.tasks.label.dto.response.LabelResponse;
import com.inpt.collaborationplatform.tasks.label.entity.Label;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {

    public LabelResponse toLabelResponse(Label label) {
        return new LabelResponse(
                label.getId(),
                label.getProject().getId(),
                label.getName(),
                label.getColor(),
                label.getCreatedAt()
        );
    }
}
