package com.example.spreado.domain.summary.application.client;

import com.example.spreado.domain.summary.application.property.AiProperties;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SummaryAiPayloadBuilder {

    private final AiProperties aiProperties;
    private final SummaryResponseFormatFactory responseFormatFactory;

    public ResponseCreateParams buildRequest(String prompt) {
        if (!StringUtils.hasText(aiProperties.getModel())) {
            throw new IllegalStateException("ai.model 설정이 필요합니다.");
        }

        return ResponseCreateParams.builder()
                .model(aiProperties.getModel())
                .input(prompt)
                .text(responseFormatFactory.getResponseTextConfig())
                .build();
    }
}
