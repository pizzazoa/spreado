package com.example.spreado.domain.meeting.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingLinkService {

    /**
     * 현재는 테스트용 더미 링크를 생성
     * 추후 구현 예정
     */
    public String generateMeetingLink(Long meetingId) {
        return "https://meeting.service/meet/" + meetingId;
    }
}
