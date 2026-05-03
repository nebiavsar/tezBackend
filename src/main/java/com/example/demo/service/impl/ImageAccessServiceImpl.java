package com.example.demo.service.impl;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.ExamSubmission;
import com.example.demo.entity.GroupAnswerKey;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ExamSubmissionRepository;
import com.example.demo.repository.GroupAnswerKeyRepository;
import com.example.demo.service.GroupManagementService;
import com.example.demo.service.ImageAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageAccessServiceImpl implements ImageAccessService {

    private final ExamSubmissionRepository examSubmissionRepository;
    private final GroupAnswerKeyRepository groupAnswerKeyRepository;
    private final GroupManagementService groupManagementService;

    @Override
    @Transactional(readOnly = true)
    public Resource getAnswerKeyImage(Long teacherId, Long groupId) {
        groupManagementService.getOwnedGroup(teacherId, groupId);
        GroupAnswerKey answerKey = groupAnswerKeyRepository.findByGroupIdAndActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("No active answer key found for this group."));
        return resolveResource(answerKey.getFilePath(), "The active answer key image could not be read from storage.");
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getExamSubmissionImage(Long teacherId, Long submissionId) {
        ExamSubmission submission = examSubmissionRepository.findByIdAndUploadedById(submissionId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam submission not found for this teacher."));
        return resolveResource(submission.getFilePath(), "The exam submission image could not be read from storage.");
    }

    private Resource resolveResource(String filePath, String message) {
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileStorageException(message, new IllegalStateException("Stored image file is missing or unreadable."));
        }
        return resource;
    }
}
