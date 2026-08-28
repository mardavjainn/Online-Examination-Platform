package com.example.exam_service.repository;

import com.example.exam_service.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByIdAndIsDeletedFalse(Long id);

    Optional<Exam> findByIdAndIsDeletedTrue(Long id);

    Page<Exam> findByIsDeletedFalse(Pageable pageable);

    Page<Exam> findByIsPublishedTrueAndIsDeletedFalse(Pageable pageable);

    Page<Exam> findByIsPublishedFalseAndIsDeletedFalse(Pageable pageable);

    Page<Exam> findByCreatedByAndIsDeletedFalse(Long createdBy, Pageable pageable);

    List<Exam> findByIsPublishedTrueAndIsDeletedFalse();

    @Query("SELECT e FROM Exam e WHERE e.isDeleted = false " +
           "AND (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR LOWER(e.category) = LOWER(:category))")
    Page<Exam> searchExams(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);

    List<Exam> findByStartTimeAfterAndIsDeletedFalse(LocalDateTime now);

    List<Exam> findByStartTimeBeforeAndEndTimeAfterAndIsDeletedFalse(LocalDateTime now1, LocalDateTime now2);
}
