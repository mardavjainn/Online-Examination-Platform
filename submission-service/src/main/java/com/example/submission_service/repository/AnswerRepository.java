package com.example.submission_service.repository;

import com.example.submission_service.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByAttemptId(Long attemptId);

    Optional<Answer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    void deleteByAttemptId(Long attemptId);
}
