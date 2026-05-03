package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ExamSubmission;

public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {

    List<ExamSubmission> findByUploadedByIdOrderByProcessedAtDesc(Long uploadedById);

    List<ExamSubmission> findByGroupIdAndUploadedByIdOrderByProcessedAtDesc(Long groupId, Long uploadedById);

    List<ExamSubmission> findByStudentIdAndUploadedByIdOrderByProcessedAtDesc(Long studentId, Long uploadedById);

    Optional<ExamSubmission> findByIdAndUploadedById(Long id, Long uploadedById);
}
