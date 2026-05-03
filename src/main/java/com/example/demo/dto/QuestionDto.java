package com.example.demo.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {

    private String questionId;

    private String questionText;

    private String extractedAnswer;

    private String expectedAnswer;

    private Integer score;

    private String feedback;

    @Builder.Default
    private Map<String, Object> additionalAttributes = new LinkedHashMap<>();

    @JsonAnySetter
    public void addAdditionalAttribute(String key, Object value) {
        additionalAttributes.put(key, value);
    }
}
