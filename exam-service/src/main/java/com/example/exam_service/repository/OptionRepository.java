package com.example.exam_service.repository;

import com.example.exam_service.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByQuestionId(Long questionId);
    List<Option> findByQuestionIdAndIsCorrectTrue(Long questionId);
}
