package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolvedGroupResponse {

    private Long id;

    private String name;

    private int examCount;

    private String answerKeyImageUrl;

    @Builder.Default
    private List<ResolvedExamResponse> exams = new ArrayList<>();
}
