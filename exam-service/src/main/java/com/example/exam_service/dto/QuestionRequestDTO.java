package com.example.exam_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    @JsonAlias({"content", "text"})
    private String questionText;

    @Valid
    private List<OptionRequestDTO> options;

    public void setContent(String content) {
        if (content != null && (this.questionText == null || this.questionText.isBlank())) {
            this.questionText = content;
        }
    }
}

