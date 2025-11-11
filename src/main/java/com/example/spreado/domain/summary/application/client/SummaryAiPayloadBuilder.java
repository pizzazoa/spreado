package com.example.spreado.domain.summary.application.client;

import com.example.spreado.domain.summary.application.property.AiProperties;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryAiPayloadBuilder {

    private final AiProperties aiProperties;
    private final SummaryResponseFormatFactory responseFormatFactory;

    public ResponseCreateParams buildRequest(String prompt) {
        log.debug("AI 요청 파라미터 빌드 시작 - promptLength: {}", prompt.length());

        if (!StringUtils.hasText(aiProperties.getModel())) {
            log.error("AI 모델 설정이 비어있음");
            throw new IllegalStateException("ai.model 설정이 필요합니다.");
        }

        log.debug("사용할 AI 모델: {}", aiProperties.getModel());

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(aiProperties.getModel())
                .input(prompt)
                .text(responseFormatFactory.getResponseTextConfig())
                .build();

        log.debug("AI 요청 파라미터 빌드 완료");
        return params;
    }
}
