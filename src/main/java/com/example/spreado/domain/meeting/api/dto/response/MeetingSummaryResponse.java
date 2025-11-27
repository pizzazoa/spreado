package com.example.spreado.domain.meeting.api.dto.response;

import com.example.spreado.domain.meeting.core.entity.MeetingStatus;

import java.time.LocalDateTime;

public record MeetingSummaryResponse(
        Long meetingId,
        Long groupId,
        String title,
        LocalDateTime createdAt,
        MeetingStatus status
) {
}
