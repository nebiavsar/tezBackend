package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.GroupAnswerKey;

public interface GroupAnswerKeyService {

    GroupAnswerKey uploadAnswerKey(Long teacherId, Long groupId, MultipartFile file);

    GroupAnswerKey getRequiredActiveAnswerKey(Long teacherId, Long groupId);
}
