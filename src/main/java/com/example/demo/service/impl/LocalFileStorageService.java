package com.example.demo.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.config.StorageProperties;
import com.example.demo.exception.FileStorageException;
import com.example.demo.service.FileStorageService;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadRoot;

    public LocalFileStorageService(StorageProperties storageProperties) {
        this.uploadRoot = Paths.get(storageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String storeExamImage(Long teacherId, Long groupId, Long studentId, MultipartFile image) {
        Path directory = uploadRoot
                .resolve("teacher-" + teacherId)
                .resolve("group-" + groupId)
                .resolve("students")
                .resolve("student-" + studentId)
                .resolve("submissions");
        return storeFile(directory, "exam-paper", image);
    }

    @Override
    public String storeAnswerKeyImage(Long teacherId, Long groupId, Integer versionNumber, MultipartFile image) {
        Path directory = uploadRoot
                .resolve("teacher-" + teacherId)
                .resolve("group-" + groupId)
                .resolve("answer-keys")
                .resolve("version-" + versionNumber);
        return storeFile(directory, "answer-key", image);
    }

    private String storeFile(Path directory, String filenamePrefix, MultipartFile image) {
        try {
            Files.createDirectories(directory);

            String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String filename = "%s-%s%s".formatted(
                    filenamePrefix,
                    UUID.randomUUID(),
                    extension == null ? "" : "." + extension);

            Path destination = directory.resolve(filename);
            image.transferTo(destination);
            return destination.toString();
        } catch (IOException exception) {
            throw new FileStorageException("Could not store the uploaded image.", exception);
        }
    }
}
