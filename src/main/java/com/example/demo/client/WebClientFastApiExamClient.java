package com.example.demo.client;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.demo.dto.FastApiProcessExamResponse;
import com.example.demo.exception.FastApiClientException;

@Component
public class WebClientFastApiExamClient implements FastApiExamClient {

    private final WebClient webClient;

    public WebClientFastApiExamClient(@Qualifier("fastApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public FastApiProcessExamResponse processExam(MultipartFile paperImage, Resource answerKeyImage) {
        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("paperImage", new NamedByteArrayResource(paperImage.getBytes(), paperImage.getOriginalFilename()))
                    .contentType(resolveMediaType(paperImage.getContentType()));
            bodyBuilder.part("answerKeyImage", answerKeyImage)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);

            FastApiProcessExamResponse response = webClient.post()
                    .uri("/process-exam")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(FastApiProcessExamResponse.class)
                    .block();

            if (response == null) {
                throw new FastApiClientException("FastAPI returned an empty response.");
            }

            return response;
        } catch (IOException exception) {
            throw new FastApiClientException("Could not read the uploaded exam image.", exception);
        } catch (WebClientResponseException exception) {
            throw new FastApiClientException("FastAPI request failed with status %s."
                    .formatted(exception.getStatusCode()), exception);
        }
    }

    private MediaType resolveMediaType(String contentType) {
        return Optional.ofNullable(contentType)
                .map(MediaType::parseMediaType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename == null ? "exam-image" : filename;
        }
    }
}
