package com.example.result_service.repository;

import com.example.result_service.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {

    Optional<Result> findByAttemptId(Long attemptId);

    List<Result> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Result> findByExamIdOrderByCreatedAtDesc(Long examId);
}
