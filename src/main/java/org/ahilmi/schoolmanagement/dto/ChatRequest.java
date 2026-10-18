package org.ahilmi.schoolmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ChatRequest(
        @NotBlank @Size(max = 8000) String message,
        @Size(max = 20) List<@NotNull @Valid Message> history
) {
    public record Message(
            @NotNull @Pattern(regexp = "user|assistant") String role,
            @NotBlank @Size(max = 16000) String content
    ) {}
}
