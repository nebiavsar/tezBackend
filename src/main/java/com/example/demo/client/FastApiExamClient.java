package com.example.demo.client;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.FastApiProcessExamResponse;

public interface FastApiExamClient {

    FastApiProcessExamResponse processExam(MultipartFile paperImage, Resource answerKeyImage);
}
