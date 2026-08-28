package com.example.exam_service.service;

import com.example.exam_service.entity.Exam;
import com.example.exam_service.entity.Option;
import com.example.exam_service.entity.Question;
import com.example.exam_service.repository.ExamRepository;
import com.example.exam_service.repository.OptionRepository;
import com.example.exam_service.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private OptionRepository optionRepository;

    @Spy
    private ExamValidationService examValidationService;

    @InjectMocks
    private ExamService examService;

    private Exam exam;
    private Question question;

    @BeforeEach
    void setUp() {
        exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Sample Exam");
        exam.setIsPublished(false);
        exam.setIsDeleted(false);

        question = new Question();
        question.setId(10L);
        question.setQuestionText("What is Java?");
        question.setExam(exam);
        question.setIsDeleted(false);

        List<Option> options = new ArrayList<>();
        Option opt1 = new Option();
        opt1.setId(101L);
        opt1.setOptionText("Programming Language");
        opt1.setIsCorrect(true);
        opt1.setIsDeleted(false);
        opt1.setQuestion(question);

        Option opt2 = new Option();
        opt2.setId(102L);
        opt2.setOptionText("Coffee");
        opt2.setIsCorrect(false);
        opt2.setIsDeleted(false);
        opt2.setQuestion(question);

        options.add(opt1);
        options.add(opt2);
        question.setOptions(options);
    }

    @Test
    void testPublishExam_Success() {
        when(examRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(exam));
        when(questionRepository.findByExamIdAndIsDeletedFalse(1L)).thenReturn(List.of(question));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        examService.publishExam(1L);

        assertTrue(exam.getIsPublished());
        verify(examRepository, times(1)).save(exam);
    }
}
