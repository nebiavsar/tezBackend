package com.example.demo.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.PostExamRequest;
import com.example.demo.dto.ResolvedExamResponse;
import com.example.demo.dto.ResolvedGroupResponse;

public interface GroupService {

    List<ResolvedGroupResponse> getGroups(Long teacherId);

    List<ResolvedGroupResponse> createGroup(
            Long teacherId,
            String name,
            List<MultipartFile> answerKeyPhotos
    );

    ResolvedExamResponse addExam(
            Long teacherId,
            Long groupId,
            PostExamRequest request,
            List<MultipartFile> examPhotos
    );
}
