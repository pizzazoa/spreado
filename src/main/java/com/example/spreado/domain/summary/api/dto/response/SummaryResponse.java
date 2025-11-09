package com.example.spreado.domain.summary.api.dto.response;

import com.example.spreado.domain.summary.core.entity.Summary;

import java.time.OffsetDateTime;

public record SummaryResponse(
        Long summaryId,
        Long noteId,
        String summaryJson
) {
    public static SummaryResponse from(Summary summary) {
        return new SummaryResponse(summary.getId(), summary.getNote().getId(), summary.getSummaryJson());
    }
}
