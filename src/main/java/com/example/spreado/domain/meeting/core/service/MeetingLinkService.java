package com.example.spreado.domain.meeting.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingLinkService {

    public String generateMeetingLink(Long meetingId) {
        // Implement meeting link generation logic here
        return "https://meeting.service/meet/" + meetingId;
    }
}
