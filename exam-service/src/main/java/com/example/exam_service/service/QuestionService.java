package com.example.exam_service.service;

import com.example.exam_service.dto.*;
import com.example.exam_service.entity.Exam;
import com.example.exam_service.entity.Option;
import com.example.exam_service.entity.Question;
import com.example.exam_service.exception.ExamNotFoundException;
import com.example.exam_service.exception.QuestionNotFoundException;
import com.example.exam_service.exception.ValidationException;
import com.example.exam_service.repository.ExamRepository;
import com.example.exam_service.repository.OptionRepository;
import com.example.exam_service.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    public QuestionService(ExamRepository examRepository,
                          QuestionRepository questionRepository,
                          OptionRepository optionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional
    public QuestionResponseDTO addQuestion(Long examId, QuestionRequestDTO request) {
        Exam exam = examRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));

        if (request.getQuestionText() == null || request.getQuestionText().trim().isEmpty()) {
            throw new ValidationException("Question text cannot be empty");
        }

        Question question = new Question();
        question.setQuestionText(request.getQuestionText().trim());
        question.setExam(exam);
        question.setIsDeleted(false);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        Question savedQuestion = questionRepository.save(question);

        if (request.getOptions() != null) {
            for (OptionRequestDTO optionRequest : request.getOptions()) {
                if (optionRequest == null) continue;
                Option option = new Option();
                option.setOptionText(optionRequest.getOptionText());
                option.setIsCorrect(Boolean.TRUE.equals(optionRequest.getIsCorrect()));
                option.setQuestion(savedQuestion);
                option.setIsDeleted(false);
                option.setCreatedAt(LocalDateTime.now());
                optionRepository.save(option);
            }
        }

        return mapToResponse(savedQuestion);
    }

    public List<QuestionResponseDTO> getQuestionsByExam(Long examId) {
        Exam exam = examRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));
        return questionRepository.findByExamIdAndIsDeletedFalse(exam.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponseDTO getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + questionId));
        if (Boolean.TRUE.equals(question.getIsDeleted())) {
            throw new QuestionNotFoundException("Question not found with id: " + questionId);
        }
        return mapToResponse(question);
    }

    @Transactional
    public QuestionResponseDTO updateQuestion(Long questionId, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + questionId));
        if (Boolean.TRUE.equals(question.getIsDeleted())) {
            throw new QuestionNotFoundException("Question not found with id: " + questionId);
        }
        question.setQuestionText(request.getQuestionText());
        question.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + questionId));
        if (Boolean.TRUE.equals(question.getIsDeleted())) {
            return;
        }
        question.setIsDeleted(true);
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);
    }

    public List<QuestionResponseDTO> searchQuestions(String keyword) {
        List<Question> questions = questionRepository.findAll().stream()
                .filter(question -> !Boolean.TRUE.equals(question.getIsDeleted()))
                .collect(Collectors.toList());
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        if (normalizedKeyword.isEmpty()) {
            return questions.stream().map(this::mapToResponse).collect(Collectors.toList());
        }
        return questions.stream()
                .filter(question -> question.getQuestionText() != null
                        && question.getQuestionText().toLowerCase().contains(normalizedKeyword))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private QuestionResponseDTO mapToResponse(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setExamId(question.getExam() != null ? question.getExam().getId() : null);

        List<OptionDTO> options = new ArrayList<>();
        if (question.getOptions() != null) {
            for (Option option : question.getOptions()) {
                if (Boolean.TRUE.equals(option.getIsDeleted())) {
                    continue;
                }
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setId(option.getId());
                optionDTO.setOptionText(option.getOptionText());
                optionDTO.setIsCorrect(option.getIsCorrect());
                options.add(optionDTO);
            }
        }
        dto.setOptions(options);
        return dto;
    }
}
