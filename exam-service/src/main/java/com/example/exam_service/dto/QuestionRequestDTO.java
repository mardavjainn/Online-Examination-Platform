package com.example.exam_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDTO {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotEmpty(message = "At least 2 options required")
    @Size(min = 2, message = "Minimum 2 options required")
    @Valid
    private List<OptionRequestDTO> options;
}
