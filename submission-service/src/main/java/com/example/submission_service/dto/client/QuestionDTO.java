package com.example.submission_service.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {
    private Long id;
    private Long examId;
    
    @JsonAlias({"content", "text"})
    private String questionText;
    
    private List<OptionDTO> options;

    @JsonProperty("content")
    public String getContent() {
        return questionText;
    }

    public void setContent(String content) {
        if (content != null && (this.questionText == null || this.questionText.isBlank())) {
            this.questionText = content;
        }
    }
}

