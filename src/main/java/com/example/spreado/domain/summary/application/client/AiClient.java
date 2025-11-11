package com.example.spreado.domain.summary.application.client;

import com.example.spreado.domain.summary.application.client.dto.MeetingSummaryDto;

public interface AiClient {
    MeetingSummaryDto requestSummary(String prompt);
}
