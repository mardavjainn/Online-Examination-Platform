package com.example.submission_service.repository;

import com.example.submission_service.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    Optional<Attempt> findByIdAndStudentId(Long id, Long studentId);

    List<Attempt> findByStudentId(Long studentId);

    List<Attempt> findByExamId(Long examId);

    Optional<Attempt> findByIdAndStatus(Long id, String status);

    Optional<Attempt> findByStudentIdAndExamIdAndStatus(Long studentId, Long examId, String status);
}
