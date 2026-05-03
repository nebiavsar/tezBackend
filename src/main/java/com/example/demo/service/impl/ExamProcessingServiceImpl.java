package com.example.demo.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.client.FastApiExamClient;
import com.example.demo.dto.ExamPaperResponse;
import com.example.demo.dto.ExamProcessResponse;
import com.example.demo.dto.FastApiProcessExamResponse;
import com.example.demo.dto.QuestionDto;
import com.example.demo.entity.ExamResult;
import com.example.demo.entity.ExamSubmission;
import com.example.demo.entity.Group;
import com.example.demo.entity.GroupAnswerKey;
import com.example.demo.entity.Student;
import com.example.demo.entity.User;
import com.example.demo.exception.FileStorageException;
import com.example.demo.repository.ExamResultRepository;
import com.example.demo.repository.ExamSubmissionRepository;
import com.example.demo.service.ExamProcessingService;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.GroupAnswerKeyService;
import com.example.demo.service.GroupManagementService;
import com.example.demo.service.StudentService;
import com.example.demo.service.UserService;
import com.example.demo.util.ImageUrlBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamProcessingServiceImpl implements ExamProcessingService {

    private final ExamResultRepository examResultRepository;
    private final ExamSubmissionRepository examSubmissionRepository;
    private final FastApiExamClient fastApiExamClient;
    private final FileStorageService fileStorageService;
    private final GroupAnswerKeyService groupAnswerKeyService;
    private final GroupManagementService groupManagementService;
    private final StudentService studentService;
    private final UserService userService;

    @Override
    @Transactional
    public ExamProcessResponse processExam(
            Long teacherId,
            Long groupId,
            Long studentId,
            MultipartFile paperImage,
            Integer teacherScore
    ) {
        validateImage(paperImage);

        User teacher = userService.getById(teacherId);
        Group group = groupManagementService.getOwnedGroup(teacherId, groupId);
        Student student = studentService.getOwnedStudent(teacherId, groupId, studentId);
        GroupAnswerKey answerKey = groupAnswerKeyService.getRequiredActiveAnswerKey(teacherId, groupId);
        Resource answerKeyImage = resolveAnswerKeyImage(answerKey);
        FastApiProcessExamResponse fastApiResponse = fastApiExamClient.processExam(paperImage, answerKeyImage);
        String storedFilePath = fileStorageService.storeExamImage(teacherId, groupId, studentId, paperImage);

        ExamSubmission submission = examSubmissionRepository.save(ExamSubmission.builder()
                .uploadedBy(teacher)
                .group(group)
                .student(student)
                .groupAnswerKey(answerKey)
                .filePath(storedFilePath)
                .originalFileName(resolveOriginalFileName(paperImage))
                .build());

        ExamResult examResult = examResultRepository.save(ExamResult.builder()
                .submission(submission)
                .extractedScore(fastApiResponse.getScore())
                .teacherScore(teacherScore)
                .build());

        return ExamProcessResponse.builder()
                .examResultId(examResult.getId())
                .submissionId(submission.getId())
                .teacherId(teacher.getId())
                .groupId(group.getId())
                .groupName(group.getName())
                .studentId(student.getId())
                .studentName(resolveStudentName(student))
                .answerKeyId(answerKey.getId())
                .answerKeyVersion(answerKey.getVersionNumber())
                .answerKeyOriginalFileName(answerKey.getOriginalFileName())
                .originalFileName(submission.getOriginalFileName())
                .examImageUrl(ImageUrlBuilder.buildExamImageUrl(submission.getId()))
                .processedAt(submission.getProcessedAt())
                .extractedScore(examResult.getExtractedScore())
                .teacherScore(examResult.getTeacherScore())
                .createdAt(examResult.getCreatedAt())
                .questions(normalizeQuestions(fastApiResponse))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamPaperResponse> getExamResults(Long teacherId, Long groupId, Long studentId) {
        if (studentId != null) {
            if (groupId == null) {
                throw new IllegalArgumentException("groupId is required when filtering by studentId.");
            }
            studentService.getOwnedStudent(teacherId, groupId, studentId);
            return examResultRepository.findBySubmissionStudentIdAndSubmissionUploadedByIdOrderByCreatedAtDesc(
                            studentId,
                            teacherId)
                    .stream()
                    .map(this::toExamPaperResponse)
                    .toList();
        }

        if (groupId != null) {
            groupManagementService.getOwnedGroup(teacherId, groupId);
            return examResultRepository.findBySubmissionGroupIdAndSubmissionUploadedByIdOrderByCreatedAtDesc(
                            groupId,
                            teacherId)
                    .stream()
                    .map(this::toExamPaperResponse)
                    .toList();
        }

        return examResultRepository.findBySubmissionUploadedByIdOrderByCreatedAtDesc(teacherId).stream()
                .map(this::toExamPaperResponse)
                .toList();
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("An exam paper image file is required.");
        }
    }

    private Resource resolveAnswerKeyImage(GroupAnswerKey answerKey) {
        Resource resource = new FileSystemResource(answerKey.getFilePath());
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileStorageException(
                    "The active answer key image could not be read from storage.",
                    new IllegalStateException("Answer key file is missing or unreadable.")
            );
        }
        return resource;
    }

    private List<QuestionDto> normalizeQuestions(FastApiProcessExamResponse fastApiResponse) {
        return fastApiResponse.getQuestions() == null ? List.of() : fastApiResponse.getQuestions();
    }

    private String resolveOriginalFileName(MultipartFile image) {
        return Optional.ofNullable(image.getOriginalFilename())
                .filter(filename -> !filename.isBlank())
                .orElse("exam-image");
    }

    private ExamPaperResponse toExamPaperResponse(ExamResult examResult) {
        ExamSubmission submission = examResult.getSubmission();
        Student student = submission.getStudent();
        Group group = submission.getGroup();
        GroupAnswerKey answerKey = submission.getGroupAnswerKey();
        return ExamPaperResponse.builder()
                .submissionId(submission.getId())
                .resultId(examResult.getId())
                .groupId(group.getId())
                .groupName(group.getName())
                .studentId(student.getId())
                .studentName(resolveStudentName(student))
                .studentNumber(student.getStudentNumber())
                .uploadedById(submission.getUploadedBy().getId())
                .answerKeyId(answerKey == null ? null : answerKey.getId())
                .answerKeyVersion(answerKey == null ? null : answerKey.getVersionNumber())
                .answerKeyOriginalFileName(answerKey == null ? null : answerKey.getOriginalFileName())
                .originalFileName(submission.getOriginalFileName())
                .filePath(submission.getFilePath())
                .examImageUrl(ImageUrlBuilder.buildExamImageUrl(submission.getId()))
                .processedAt(submission.getProcessedAt())
                .createdAt(examResult.getCreatedAt())
                .extractedScore(examResult.getExtractedScore())
                .teacherScore(examResult.getTeacherScore())
                .build();
    }

    private String resolveStudentName(Student student) {
        return (student.getFirstName() + " " + student.getLastName()).trim();
    }
}
