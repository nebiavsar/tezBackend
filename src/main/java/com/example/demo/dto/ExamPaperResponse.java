package com.example.demo.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamPaperResponse {

    private Long submissionId;
    private Long resultId;
    private Long groupId;
    private String groupName;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Long uploadedById;
    private Long answerKeyId;
    private Integer answerKeyVersion;
    private String answerKeyOriginalFileName;
    private String originalFileName;
    private String filePath;
    private String examImageUrl;
    private Instant processedAt;
    private Instant createdAt;
    private Integer extractedScore;
    private Integer teacherScore;
}
