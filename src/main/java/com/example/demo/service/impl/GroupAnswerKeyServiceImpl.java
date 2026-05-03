package com.example.demo.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Group;
import com.example.demo.entity.GroupAnswerKey;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.GroupAnswerKeyRepository;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.GroupAnswerKeyService;
import com.example.demo.service.GroupManagementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupAnswerKeyServiceImpl implements GroupAnswerKeyService {

    private final GroupAnswerKeyRepository groupAnswerKeyRepository;
    private final GroupManagementService groupManagementService;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public GroupAnswerKey uploadAnswerKey(Long teacherId, Long groupId, MultipartFile file) {
        validateFile(file);

        Group group = groupManagementService.getOwnedGroup(teacherId, groupId);
        int nextVersion = groupAnswerKeyRepository.findTopByGroupIdOrderByVersionNumberDesc(groupId)
                .map(answerKey -> answerKey.getVersionNumber() + 1)
                .orElse(1);

        String storedFilePath = fileStorageService.storeAnswerKeyImage(teacherId, groupId, nextVersion, file);

        groupAnswerKeyRepository.findByGroupIdAndActiveTrue(groupId)
                .ifPresent(existingAnswerKey -> {
                    existingAnswerKey.setActive(false);
                    groupAnswerKeyRepository.save(existingAnswerKey);
                });

        return groupAnswerKeyRepository.save(GroupAnswerKey.builder()
                .group(group)
                .filePath(storedFilePath)
                .originalFileName(resolveOriginalFileName(file))
                .versionNumber(nextVersion)
                .active(true)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public GroupAnswerKey getRequiredActiveAnswerKey(Long teacherId, Long groupId) {
        groupManagementService.getOwnedGroup(teacherId, groupId);
        return findActiveAnswerKey(groupId);
    }

    private GroupAnswerKey findActiveAnswerKey(Long groupId) {
        return groupAnswerKeyRepository.findByGroupIdAndActiveTrue(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("No active answer key found for this group."));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A group answer key image file is required.");
        }
    }

    private String resolveOriginalFileName(MultipartFile file) {
        return Optional.ofNullable(file.getOriginalFilename())
                .filter(filename -> !filename.isBlank())
                .orElse("group-answer-key-image");
    }
}
