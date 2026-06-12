package com.inpt.collaborationplatform.collaboration.attachment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAttachmentRequest {
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must be at most 255 characters")
    private String fileName;

    @NotBlank(message = "File URL is required")
    @Size(max = 2000, message = "File URL must be at most 2000 characters")
    private String fileUrl;

    @Positive(message = "File size must be positive")
    private long fileSize;
}
