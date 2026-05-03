package com.example.demo.service;

import org.springframework.core.io.Resource;

public interface ImageAccessService {

    Resource getAnswerKeyImage(Long teacherId, Long groupId);

    Resource getExamSubmissionImage(Long teacherId, Long submissionId);
}
