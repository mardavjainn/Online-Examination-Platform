package com.example.exam_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionResponseDTO {
    private Long id;
    private Long questionId;
    private String optionText;
    private Boolean isCorrect;
}
