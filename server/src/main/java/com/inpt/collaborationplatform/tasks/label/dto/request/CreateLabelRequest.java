package com.inpt.collaborationplatform.tasks.label.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLabelRequest {
    @NotBlank(message = "Label name is required")
    @Size(max = 60, message = "Label name must be at most 60 characters")
    private String name;

    @NotBlank(message = "Label color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color (e.g. #FF5733)")
    private String color;
}
