package com.example.exam_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionRequestDTO {

    @NotBlank(message = "Option text is required")
    private String optionText;

    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;
}
