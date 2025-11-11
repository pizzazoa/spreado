package com.example.spreado.domain.meeting.core.util;

import com.example.spreado.domain.meeting.core.entity.Meeting;
import org.springframework.stereotype.Component;

@Component
public class RoomIdPolicy {
    public String toRoomId(Meeting meeting) {
        Long groupId = meeting.getGroup().getId();
        Long meetingId = meeting.getId();
        return "group:" + groupId + ":meeting:" + meetingId;
    }
}