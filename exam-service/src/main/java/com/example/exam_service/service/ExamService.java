package com.example.exam_service.service;

import com.example.exam_service.dto.*;
import com.example.exam_service.entity.Exam;
import com.example.exam_service.entity.Option;
import com.example.exam_service.entity.Question;
import com.example.exam_service.exception.ExamNotFoundException;
import com.example.exam_service.exception.ValidationException;
import com.example.exam_service.repository.ExamRepository;
import com.example.exam_service.repository.QuestionRepository;
import com.example.exam_service.repository.OptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final ExamValidationService examValidationService;

    public ExamService(ExamRepository examRepository,
                      QuestionRepository questionRepository,
                      OptionRepository optionRepository,
                      ExamValidationService examValidationService) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.examValidationService = examValidationService;
    }

    public ExamResponseDTO createExam(ExamRequestDTO request) {
        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setCategory(request.getCategory());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setDuration(request.getDuration());
        Long creator = request.getCreatedBy() != null ? request.getCreatedBy() : 1L;
        exam.setCreatedBy(creator);
        exam.setUpdatedBy(creator);
        boolean published = request.getIsPublished() != null ? request.getIsPublished() : true;
        exam.setIsPublished(published);
        exam.setIsDeleted(false);
        exam.setCreatedAt(LocalDateTime.now());
        exam.setUpdatedAt(LocalDateTime.now());
        Exam savedExam = examRepository.save(exam);
        return mapToResponse(savedExam);
    }

    public Page<ExamResponseDTO> getAllExams(Pageable pageable) {
        return examRepository.findByIsDeletedFalse(pageable).map(this::mapToResponse);
    }

    public List<ExamResponseDTO> getPublishedExams() {
        return examRepository.findByIsPublishedTrueAndIsDeletedFalse().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ExamResponseDTO> getDraftExams() {
        return examRepository.findByIsPublishedFalseAndIsDeletedFalse(Pageable.unpaged()).getContent().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ExamResponseDTO getExamById(Long examId) {
        Exam exam = examRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));
        return mapToResponse(exam);
    }

    @Transactional
    public ExamResponseDTO updateExam(Long examId, ExamUpdateRequest request) {
        Exam exam = examRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            exam.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            exam.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            exam.setCategory(request.getCategory());
        }
        if (request.getStartTime() != null) {
            exam.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            exam.setEndTime(request.getEndTime());
        }
        if (request.getDuration() != null) {
            exam.setDuration(request.getDuration());
        }
        if (request.getIsPublished() != null) {
            exam.setIsPublished(request.getIsPublished());
        }

        exam.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(examRepository.save(exam));
    }

    @Transactional
    public void publishExam(Long examId) {
        Exam exam = examRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));

        if (exam.getTitle() == null || exam.getTitle().trim().isEmpty()) {
            throw new ValidationException("Exam title cannot be empty");
        }

        List<Question> questions = questionRepository.findByExamIdAndIsDeletedFalse(exam.getId());
        examValidationService.validateExamBeforePublish(exam, questions);
        exam.setIsPublished(true);
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
    }

    @Transactional
    public void unpublishExam(Long examId) {
        Exam exam = examRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));
        exam.setIsPublished(false);
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(Long examId) {
        Exam exam = examRepository.findByIdAndIsDeletedFalse(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));
        exam.setIsDeleted(true);
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
    }

    public List<ExamResponseDTO> searchExams(String keyword) {
        List<Exam> exams = examRepository.findByIsDeletedFalse(Pageable.unpaged()).getContent();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        if (normalizedKeyword.isEmpty()) {
            return exams.stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        return exams.stream()
                .filter(exam -> (exam.getTitle() != null && exam.getTitle().toLowerCase().contains(normalizedKeyword))
                        || (exam.getDescription() != null && exam.getDescription().toLowerCase().contains(normalizedKeyword)))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExamStatsDTO getExamStats() {
        List<Exam> exams = examRepository.findByIsDeletedFalse(Pageable.unpaged()).getContent();
        long totalExams = exams.size();
        long publishedExams = exams.stream().filter(exam -> Boolean.TRUE.equals(exam.getIsPublished())).count();
        long draftExams = totalExams - publishedExams;
        long totalQuestions = 0;
        long totalOptions = 0;

        for (Exam exam : exams) {
            List<Question> questions = questionRepository.findByExamIdAndIsDeletedFalse(exam.getId());
            totalQuestions += questions.size();
            for (Question question : questions) {
                totalOptions += optionRepository.findByQuestionId(question.getId()).size();
            }
        }

        return new ExamStatsDTO(totalExams, publishedExams, draftExams, totalQuestions, totalOptions);
    }

    private ExamResponseDTO mapToResponse(Exam exam) {
        ExamResponseDTO dto = new ExamResponseDTO();
        dto.setId(exam.getId());
        dto.setTitle(exam.getTitle());
        dto.setDescription(exam.getDescription());
        dto.setIsPublished(exam.getIsPublished());
        dto.setCreatedAt(exam.getCreatedAt());
        dto.setUpdatedAt(exam.getUpdatedAt());
        dto.setCreatedBy(exam.getCreatedBy());
        dto.setUpdatedBy(exam.getUpdatedBy());
        dto.setCategory(exam.getCategory());
        dto.setStartTime(exam.getStartTime());
        dto.setEndTime(exam.getEndTime());
        dto.setDuration(exam.getDuration());
        dto.setVersion(exam.getVersion());

        List<Question> questions = questionRepository.findByExamIdAndIsDeletedFalse(exam.getId());
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (Question q : questions) {
            QuestionDTO qDto = new QuestionDTO();
            qDto.setId(q.getId());
            qDto.setQuestionText(q.getQuestionText());

            List<Option> options = optionRepository.findByQuestionId(q.getId());
            List<OptionDTO> optionDTOs = new ArrayList<>();
            for (Option option : options) {
                if (Boolean.TRUE.equals(option.getIsDeleted())) {
                    continue;
                }
                OptionDTO optionDTO = new OptionDTO();
                optionDTO.setId(option.getId());
                optionDTO.setOptionText(option.getOptionText());
                optionDTO.setIsCorrect(option.getIsCorrect());
                optionDTOs.add(optionDTO);
            }
            qDto.setOptions(optionDTOs);
            questionDTOs.add(qDto);
        }
        dto.setQuestions(questionDTOs);
        dto.setTotalQuestions(questionDTOs.size());
        dto.setTotalOptions(questionDTOs.stream().mapToInt(q -> q.getOptions() == null ? 0 : q.getOptions().size()).sum());
        return dto;
    }
}
