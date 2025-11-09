package com.example.spreado.domain.summary.application.client;

import com.example.spreado.domain.summary.application.property.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SummaryAiPayloadBuilder {

    private final AiProperties aiProperties;
    private final SummaryResponseFormatFactory responseFormatFactory;

    public Map<String, Object> buildPayload(String prompt) {
        if (!StringUtils.hasText(aiProperties.getModel())) {
            throw new IllegalStateException("ai.model 설정이 필요합니다.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProperties.getModel());
        payload.put("input", prompt);
        payload.put("response_format", responseFormatFactory.getResponseFormat());
        return payload;
    }
}
