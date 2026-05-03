package com.example.demo.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamProcessResponse {

    private Long examResultId;

    private Long submissionId;

    private Long teacherId;

    private Long groupId;

    private String groupName;

    private Long studentId;

    private String studentName;

    private Long answerKeyId;

    private Integer answerKeyVersion;

    private String answerKeyOriginalFileName;

    private String originalFileName;

    private String examImageUrl;

    private Instant processedAt;

    private Integer extractedScore;

    private Integer teacherScore;

    private Instant createdAt;

    @Builder.Default
    private List<QuestionDto> questions = new ArrayList<>();
}
