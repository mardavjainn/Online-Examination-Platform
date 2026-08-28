package com.example.exam_service.service;

import com.example.exam_service.dto.OptionDTO;
import com.example.exam_service.dto.OptionRequestDTO;
import com.example.exam_service.dto.OptionResponseDTO;
import com.example.exam_service.dto.OptionUpdateRequest;
import com.example.exam_service.entity.Option;
import com.example.exam_service.entity.Question;
import com.example.exam_service.exception.QuestionNotFoundException;
import com.example.exam_service.exception.ValidationException;
import com.example.exam_service.repository.OptionRepository;
import com.example.exam_service.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OptionService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    public OptionService(QuestionRepository questionRepository, OptionRepository optionRepository) {
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional
    public OptionResponseDTO addOption(Long questionId, OptionRequestDTO request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + questionId));

        if (request.getOptionText() == null || request.getOptionText().trim().isEmpty()) {
            throw new ValidationException("Option text cannot be empty");
        }
        if (request.getIsCorrect() == null) {
            throw new ValidationException("isCorrect flag must be provided");
        }

        Option option = new Option();
        option.setOptionText(request.getOptionText().trim());
        option.setIsCorrect(request.getIsCorrect());
        option.setQuestion(question);
        option.setIsDeleted(false);
        option.setCreatedAt(LocalDateTime.now());
        Option saved = optionRepository.save(option);

        return mapToResponse(saved);
    }

    public List<OptionResponseDTO> getOptionsByQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + questionId));
        return optionRepository.findByQuestionId(question.getId()).stream()
                .filter(option -> !Boolean.TRUE.equals(option.getIsDeleted()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OptionResponseDTO updateOption(Long optionId, OptionUpdateRequest request) {
        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new QuestionNotFoundException("Option not found with id: " + optionId));
        if (Boolean.TRUE.equals(option.getIsDeleted())) {
            throw new QuestionNotFoundException("Option not found with id: " + optionId);
        }
        if (request.getOptionText() == null || request.getOptionText().trim().isEmpty()) {
            throw new ValidationException("Option text cannot be empty");
        }
        if (request.getIsCorrect() == null) {
            throw new ValidationException("isCorrect flag must be provided");
        }

        option.setOptionText(request.getOptionText().trim());
        option.setIsCorrect(request.getIsCorrect());
        return mapToResponse(optionRepository.save(option));
    }

    @Transactional
    public void deleteOption(Long optionId) {
        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new QuestionNotFoundException("Option not found with id: " + optionId));
        if (Boolean.TRUE.equals(option.getIsDeleted())) {
            return;
        }
        option.setIsDeleted(true);
        optionRepository.save(option);
    }

    private OptionResponseDTO mapToResponse(Option option) {
        OptionResponseDTO dto = new OptionResponseDTO();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setIsCorrect(option.getIsCorrect());
        dto.setQuestionId(option.getQuestion() != null ? option.getQuestion().getId() : null);
        return dto;
    }
}
