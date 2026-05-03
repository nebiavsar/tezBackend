package com.example.demo.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.AppUserPrincipal;
import com.example.demo.service.ImageAccessService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageAccessService imageAccessService;

    @GetMapping("/classes/{groupId}/answer-key/image")
    public ResponseEntity<Resource> getActiveAnswerKeyImage(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long groupId) {
        Resource resource = imageAccessService.getAnswerKeyImage(principal.getId(), groupId);
        return buildImageResponse(resource);
    }

    @GetMapping("/exams/submissions/{submissionId}/image")
    public ResponseEntity<Resource> getExamSubmissionImage(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long submissionId) {
        Resource resource = imageAccessService.getExamSubmissionImage(principal.getId(), submissionId);
        return buildImageResponse(resource);
    }

    private ResponseEntity<Resource> buildImageResponse(Resource resource) {
        MediaType mediaType = MediaTypeFactory.getMediaType(resource.getFilename())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
