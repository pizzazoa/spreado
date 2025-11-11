package com.example.spreado.domain.meeting.api.dto.response;

import com.example.spreado.domain.meeting.core.entity.MeetingStatus;

import java.util.List;

public record MeetingDetailResponse(
        Long meetingId,
        String title,
        MeetingStatus status,
        List<MeetingMemberResponse> members
) {
}
