package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolvedExamResponse {

    private String fullName;

    private String no;

    private Double score;

    private String examImageUrl;
}
