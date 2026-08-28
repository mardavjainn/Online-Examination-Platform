package com.example.exam_service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO {
    private Long id;
    private String optionText;
    private Boolean isCorrect;
}
