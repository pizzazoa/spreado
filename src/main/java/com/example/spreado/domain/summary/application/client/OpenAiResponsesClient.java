package com.example.spreado.domain.summary.application.client;

import com.example.spreado.domain.summary.application.client.dto.MeetingSummaryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.errors.OpenAIException;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiResponsesClient implements AiClient {

    private final SummaryAiPayloadBuilder payloadBuilder;
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    @Override
    public MeetingSummaryDto requestSummary(String prompt) {
        ResponseCreateParams params = payloadBuilder.buildRequest(prompt);

        try {
            Response response = openAIClient.responses().create(params);

            // 에러 응답 확인
            response.error().ifPresent(error -> {
                log.error("AI 응답 에러 - code: {}, message: {}",
                        error.code(), error.message());
                throw new IllegalStateException("AI 응답 실패: " + error.message());
            });

            String jsonText = extractText(response);
            return parseToDto(jsonText);

        } catch (OpenAIException e) {
            log.error("OpenAI API 호출 중 오류 발생 - errorType: {}, message: {}",
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw new IllegalStateException("AI 요약 호출 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생 - errorType: {}, message: {}",
                    e.getClass().getName(), e.getMessage(), e);
            throw new IllegalStateException("AI 요약 호출 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Response 객체에서 텍스트 콘텐츠를 추출합니다.
     */
    private String extractText(Response response) {
        return response.output().stream()
                .map(ResponseOutputItem::message)
                .flatMap(Optional::stream)
                .map(ResponseOutputMessage::content)
                .flatMap(java.util.Collection::stream)
                .map(ResponseOutputMessage.Content::outputText)
                .flatMap(Optional::stream)
                .map(ResponseOutputText::text)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("AI 응답에 텍스트가 포함되어 있지 않습니다."));
    }

    /**
     * JSON 텍스트를 MeetingSummaryDto로 파싱합니다.
     * Structured Outputs를 사용하므로 항상 유효한 JSON이 보장됩니다.
     */
    private MeetingSummaryDto parseToDto(String jsonText) {
        try {
            return objectMapper.readValue(jsonText, MeetingSummaryDto.class);
        } catch (JsonProcessingException e) {
            log.error("AI 응답 JSON 파싱 실패. 응답: {}", jsonText, e);
            throw new IllegalStateException("AI 응답을 파싱하는 중 오류가 발생했습니다.", e);
        }
    }
}
