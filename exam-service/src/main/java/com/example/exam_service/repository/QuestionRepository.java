package com.example.exam_service.repository;

import com.example.exam_service.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamId(Long examId);
    List<Question> findByExamIdAndIsDeletedFalse(Long examId);
}
