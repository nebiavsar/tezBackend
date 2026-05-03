package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.PostExamRequest;
import com.example.demo.dto.ResolvedExamResponse;
import com.example.demo.dto.ResolvedGroupResponse;
import com.example.demo.security.AppUserPrincipal;
import com.example.demo.service.GroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<ResolvedGroupResponse>> getGroups(
            @AuthenticationPrincipal AppUserPrincipal principal) {
        return ResponseEntity.ok(groupService.getGroups(principal.getId()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResolvedGroupResponse>> createGroup(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestPart("name") String name,
            @RequestPart("answerKeyPhotos") List<MultipartFile> answerKeyPhotos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(principal.getId(), name, answerKeyPhotos));
    }

    @PutMapping(value = "/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResolvedExamResponse> addExam(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long groupId,
            @Valid @RequestPart("postExamDTO") PostExamRequest postExamDTO,
            @RequestPart("examPhotos") List<MultipartFile> examPhotos) {
        return ResponseEntity.ok(groupService.addExam(principal.getId(), groupId, postExamDTO, examPhotos));
    }
}
