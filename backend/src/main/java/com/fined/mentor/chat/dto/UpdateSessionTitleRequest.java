package com.fined.mentor.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSessionTitleRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;
}
