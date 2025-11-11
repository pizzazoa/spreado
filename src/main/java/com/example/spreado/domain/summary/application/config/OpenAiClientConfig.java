package com.example.spreado.domain.summary.application.config;

import com.example.spreado.domain.summary.application.property.AiProperties;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class OpenAiClientConfig {

    private final AiProperties aiProperties;

    @Bean
    public OpenAIClient openAIClient() {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new IllegalStateException("ai.api-key 설정이 필요합니다.");
        }

        OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder()
                .apiKey(aiProperties.getApiKey())
                .responseValidation(true)
                .timeout(Duration.ofSeconds(120));  // 읽기 타임아웃 120초

        if (StringUtils.hasText(aiProperties.getUrl())) {
            builder.baseUrl(aiProperties.getUrl());
        }

        return builder.build();
    }
}
