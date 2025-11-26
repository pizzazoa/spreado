package com.example.spreado.domain.meeting.api.dto.response;

public record MeetingCreateResponse(
        Long meetingId,
        String token
) {
}