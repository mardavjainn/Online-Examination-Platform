package com.example.exam_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionUpdateRequest {

    @NotBlank(message = "Option text is required")
    private String optionText;

    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;
}
