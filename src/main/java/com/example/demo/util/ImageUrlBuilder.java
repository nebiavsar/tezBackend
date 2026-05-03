package com.example.demo.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public final class ImageUrlBuilder {

    private ImageUrlBuilder() {
    }

    public static String buildExamImageUrl(Long submissionId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/exams/submissions/{submissionId}/image")
                .buildAndExpand(submissionId)
                .toUriString();
    }

    public static String buildAnswerKeyImageUrl(Long groupId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/classes/{groupId}/answer-key/image")
                .buildAndExpand(groupId)
                .toUriString();
    }
}
