package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeExamImage(Long teacherId, Long groupId, Long studentId, MultipartFile image);

    String storeAnswerKeyImage(Long teacherId, Long groupId, Integer versionNumber, MultipartFile image);
}
