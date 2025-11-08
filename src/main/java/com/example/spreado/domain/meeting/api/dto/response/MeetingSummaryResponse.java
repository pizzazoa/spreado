package com.example.spreado.domain.meeting.api.dto.response;

import com.example.spreado.domain.meeting.core.entity.MeetingStatus;

public record MeetingSummaryResponse(
        Long meetingId,
        String title,
        String meetingLink,
        MeetingStatus status
) {
}
