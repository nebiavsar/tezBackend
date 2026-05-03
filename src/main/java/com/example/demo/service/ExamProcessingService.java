package com.example.demo.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ExamPaperResponse;
import com.example.demo.dto.ExamProcessResponse;

public interface ExamProcessingService {

    ExamProcessResponse processExam(
            Long teacherId,
            Long groupId,
            Long studentId,
            MultipartFile paperImage,
            Integer teacherScore
    );

    List<ExamPaperResponse> getExamResults(Long teacherId, Long groupId, Long studentId);
}
