package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ExamPaperResponse;
import com.example.demo.dto.ExamProcessResponse;
import com.example.demo.dto.PostExamRequest;
import com.example.demo.dto.ResolvedExamResponse;
import com.example.demo.dto.ResolvedGroupResponse;
import com.example.demo.entity.Group;
import com.example.demo.entity.Student;
import com.example.demo.repository.GroupAnswerKeyRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.service.ExamProcessingService;
import com.example.demo.service.GroupService;
import com.example.demo.service.GroupAnswerKeyService;
import com.example.demo.service.GroupManagementService;
import com.example.demo.service.StudentService;
import com.example.demo.util.FullNameParser;
import com.example.demo.util.ImageUrlBuilder;
import com.example.demo.util.MultipartImageMergeUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final ExamProcessingService examProcessingService;
    private final GroupAnswerKeyRepository groupAnswerKeyRepository;
    private final GroupAnswerKeyService groupAnswerKeyService;
    private final GroupManagementService groupManagementService;
    private final StudentRepository studentRepository;
    private final StudentService studentService;

    @Override
    @Transactional(readOnly = true)
    public List<ResolvedGroupResponse> getGroups(Long teacherId) {
        return groupManagementService.getTeacherGroups(teacherId).stream()
                .map(group -> toResolvedGroup(
                        group,
                        examProcessingService.getExamResults(teacherId, group.getId(), null)))
                .toList();
    }

    @Override
    @Transactional
    public List<ResolvedGroupResponse> createGroup(
            Long teacherId,
            String name,
            List<MultipartFile> answerKeyPhotos
    ) {
        MultipartFile answerKey = MultipartImageMergeUtil.mergeVertically(answerKeyPhotos, "file", "answer key photo");
        Long groupId = groupManagementService.createGroup(teacherId, requireText(name, "name")).getId();
        groupAnswerKeyService.uploadAnswerKey(teacherId, groupId, answerKey);
        return getGroups(teacherId);
    }

    @Override
    @Transactional
    public ResolvedExamResponse addExam(
            Long teacherId,
            Long groupId,
            PostExamRequest request,
            List<MultipartFile> examPhotos
    ) {
        MultipartFile examPhoto = MultipartImageMergeUtil.mergeVertically(examPhotos, "image", "exam photo");
        PostExamRequest normalizedRequest = normalizeRequest(request);

        Student student = studentRepository.findByGroupIdAndStudentNumber(groupId, normalizedRequest.getNo())
                .orElseGet(() -> createStudent(teacherId, groupId, normalizedRequest));

        ExamProcessResponse response = examProcessingService.processExam(
                teacherId,
                groupId,
                student.getId(),
                examPhoto,
                null);

        return ResolvedExamResponse.builder()
                .fullName(normalizedRequest.getFullName())
                .no(normalizedRequest.getNo())
                .score(resolveScore(response.getTeacherScore(), response.getExtractedScore()))
                .examImageUrl(response.getExamImageUrl())
                .build();
    }

    private Student createStudent(Long teacherId, Long groupId, PostExamRequest request) {
        FullNameParser.NameParts nameParts = FullNameParser.parse(request.getFullName());
        return studentService.createStudent(
                teacherId,
                groupId,
                request.getNo(),
                nameParts.firstName(),
                nameParts.lastName());
    }

    private ResolvedGroupResponse toResolvedGroup(Group group, List<ExamPaperResponse> examResults) {
        List<ResolvedExamResponse> exams = examResults.stream()
                .map(this::toResolvedExam)
                .toList();

        return ResolvedGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .examCount(exams.size())
                .answerKeyImageUrl(resolveAnswerKeyImageUrl(group.getId()))
                .exams(exams)
                .build();
    }

    private ResolvedExamResponse toResolvedExam(ExamPaperResponse examPaperResponse) {
        return ResolvedExamResponse.builder()
                .fullName(examPaperResponse.getStudentName())
                .no(examPaperResponse.getStudentNumber())
                .score(resolveScore(examPaperResponse.getTeacherScore(), examPaperResponse.getExtractedScore()))
                .examImageUrl(examPaperResponse.getExamImageUrl())
                .build();
    }

    private String resolveAnswerKeyImageUrl(Long groupId) {
        return groupAnswerKeyRepository.findByGroupIdAndActiveTrue(groupId)
                .map(answerKey -> ImageUrlBuilder.buildAnswerKeyImageUrl(answerKey.getGroup().getId()))
                .orElse(null);
    }

    private PostExamRequest normalizeRequest(PostExamRequest request) {
        PostExamRequest normalizedRequest = new PostExamRequest();
        normalizedRequest.setFullName(requireText(request == null ? null : request.getFullName(), "fullName"));
        normalizedRequest.setNo(requireText(request == null ? null : request.getNo(), "no"));
        return normalizedRequest;
    }

    private String requireText(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return normalized;
    }

    private Double resolveScore(Integer teacherScore, Integer extractedScore) {
        Integer score = teacherScore == null ? extractedScore : teacherScore;
        return score == null ? null : score.doubleValue();
    }
}
