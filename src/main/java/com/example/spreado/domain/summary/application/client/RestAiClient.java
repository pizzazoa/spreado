package com.example.spreado.domain.summary.application.client;

import com.example.spreado.domain.summary.application.property.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class RestAiClient implements AiClient {

    private final SummaryAiPayloadBuilder payloadBuilder;
    private final AiProperties aiProperties;

    @Override
    public String requestSummary(String prompt) {
        if (!StringUtils.hasText(aiProperties.getUrl())) {
            throw new IllegalStateException("ai.url 설정이 필요합니다.");
        }

        RestClient.RequestBodySpec requestSpec = RestClient.create()
                .post()
                .uri(aiProperties.getUrl())
                .contentType(MediaType.APPLICATION_JSON);

        if (StringUtils.hasText(aiProperties.getApiKey())) {
            requestSpec = requestSpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey());
        }

        try {
            return requestSpec.body(payloadBuilder.buildPayload(prompt))
                    .retrieve().body(String.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("AI 요약 호출 중 오류가 발생했습니다.", e);
        }
    }
}
