package com.example.spreado.domain.summary.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PromptService {

    private final String summaryTemplate;

    public PromptService(@Value("${summary.prompt.template:}") String summaryTemplate) {
        this.summaryTemplate = summaryTemplate;
    }

    public String buildSummaryPrompt(String meetingBody) {
        if (!StringUtils.hasText(summaryTemplate)) {
            throw new IllegalStateException("summary.prompt.template 설정이 필요합니다.");
        }
        return summaryTemplate.formatted(meetingBody);
    }
}
