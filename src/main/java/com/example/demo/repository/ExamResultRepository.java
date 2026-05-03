package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ExamResult;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findBySubmissionUploadedByIdOrderByCreatedAtDesc(Long uploadedById);

    List<ExamResult> findBySubmissionGroupIdAndSubmissionUploadedByIdOrderByCreatedAtDesc(
            Long groupId,
            Long uploadedById
    );

    List<ExamResult> findBySubmissionStudentIdAndSubmissionUploadedByIdOrderByCreatedAtDesc(
            Long studentId,
            Long uploadedById
    );
}
