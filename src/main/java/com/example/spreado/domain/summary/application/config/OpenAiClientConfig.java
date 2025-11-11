package com.example.spreado.domain.summary.application.config;

import com.example.spreado.domain.summary.application.property.AiProperties;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenAiClientConfig {

    private final AiProperties aiProperties;

    @Bean
    public OpenAIClient openAIClient() {
        log.info("OpenAI 클라이언트 초기화 시작");

        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            log.error("OpenAI API 키가 설정되지 않음");
            throw new IllegalStateException("ai.api-key 설정이 필요합니다.");
        }

        log.info("OpenAI API 키 확인 완료 (길이: {})", aiProperties.getApiKey().length());

        // OpenAI 클라이언트 설정 (타임아웃 등)
        // AI 응답은 시간이 걸릴 수 있으므로 충분한 타임아웃 설정
        OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder()
                .apiKey(aiProperties.getApiKey())
                .responseValidation(true)
                .timeout(Duration.ofSeconds(120));  // 읽기 타임아웃 120초

        if (StringUtils.hasText(aiProperties.getUrl())) {
            log.info("커스텀 OpenAI URL 사용: {}", aiProperties.getUrl());
            builder.baseUrl(aiProperties.getUrl());
        } else {
            log.info("기본 OpenAI URL 사용");
        }

        OpenAIClient client = builder.build();
        log.info("OpenAI 클라이언트 초기화 완료");

        return client;
    }
}
