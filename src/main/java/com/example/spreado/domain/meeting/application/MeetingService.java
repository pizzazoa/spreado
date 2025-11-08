package com.example.spreado.domain.meeting.application;

import com.example.spreado.domain.group.core.entity.Group;
import com.example.spreado.domain.group.core.repository.GroupMemberRepository;
import com.example.spreado.domain.group.core.repository.GroupRepository;
import com.example.spreado.domain.meeting.api.dto.request.MeetingCreateRequest;
import com.example.spreado.domain.meeting.api.dto.response.*;
import com.example.spreado.domain.meeting.core.entity.Meeting;
import com.example.spreado.domain.meeting.core.entity.MeetingJoin;
import com.example.spreado.domain.meeting.core.entity.MeetingStatus;
import com.example.spreado.domain.note.core.entity.Note;
import com.example.spreado.domain.meeting.core.repository.MeetingJoinRepository;
import com.example.spreado.domain.meeting.core.repository.MeetingRepository;
import com.example.spreado.domain.meeting.core.service.MeetingLinkService;
import com.example.spreado.domain.note.core.service.NoteService;
import com.example.spreado.domain.note.api.dto.response.NoteResponse;
import com.example.spreado.domain.user.core.entity.User;
import com.example.spreado.domain.user.core.repository.UserRepository;
import com.example.spreado.global.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingJoinRepository meetingJoinRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MeetingLinkService meetingLinkService;
    private final NoteService noteService;

    public MeetingCreateResponse createMeeting(@Valid MeetingCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        Meeting meeting = Meeting.create(group, user, request.title());
        meetingRepository.save(meeting);

        MeetingJoin hostJoin = MeetingJoin.create(meeting, user);
        meetingJoinRepository.save(hostJoin);

        String meetingLink = meetingLinkService.generateMeetingLink(meeting.getId());
        meetingRepository.setMeetingLink(meeting.getId(), meetingLink);

        return new MeetingCreateResponse(meeting.getId(), meetingLink);
    }

    public MeetingJoinResponse joinMeeting(Long meetingId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        Group group = meeting.getGroup();

        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new NotFoundException("해당 그룹의 멤버가 아니므로 회의에 참여할 수 없습니다.");
        }

        if (meetingJoinRepository.existsByMeetingIdAndUserId(meeting.getId(), userId)) {
            throw new NotFoundException("이미 회의에 참여한 상태입니다.");
        }

        MeetingJoin meetingJoin = MeetingJoin.create(meeting, user);
        meetingJoinRepository.save(meetingJoin);
        return new MeetingJoinResponse(meeting.getId(), userId);
    }

    public void leaveMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        MeetingJoin membership = meetingJoinRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new NotFoundException("해당 회의 참여 정보를 찾을 수 없습니다."));

        if (membership.getMeeting().getCreator().getId().equals(userId)) {
            throw new NotFoundException("호스트는 회의에서 나갈 수 없습니다.");
        }

        meetingJoinRepository.deleteById(membership.getId());
    }

    public List<MeetingSummaryResponse> getMeetingsByGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        List<Meeting> meetings = meetingRepository.findAllByGroupId(group.getId());

        return meetings.stream()
                .map(meeting -> new MeetingSummaryResponse(
                        meeting.getId(),
                        group.getId(),
                        meeting.getTitle(),
                        meeting.getMeetingLink(),
                        meeting.getStatus()
                ))
                .toList();
    }

    public MeetingDetailResponse getMeetingDetail(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        List<MeetingJoin> participants = meetingJoinRepository.findAllByMeetingId(meeting.getId());

        List<MeetingMemberResponse> participantResponses = participants.stream()
                .map(participant -> new MeetingMemberResponse(
                        participant.getUser().getId(),
                        participant.getUser().getName()
                ))
                .toList();

        return new MeetingDetailResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getMeetingLink(),
                meeting.getStatus(),
                participantResponses
        );
    }

    public NoteResponse endMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        if (!meeting.getCreator().getId().equals(userId)) {
            throw new NotFoundException("호스트만 회의를 종료할 수 있습니다.");
        }

        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new NotFoundException("이미 종료된 회의입니다.");
        }

        Note note = noteService.generateNoteForMeeting(meeting);

        meeting.endMeeting();

        return new NoteResponse(
                note.getId(),
                meeting.getId(),
                note.getContent()
        );
    }

    public List<MeetingSummaryResponse> getMyMeetings(Long userId) {
        List<MeetingJoin> memberships = meetingJoinRepository.findAllByUserId(userId);

        return memberships.stream()
                .map(member -> {
                    Meeting meeting = member.getMeeting();
                    return new MeetingSummaryResponse(
                            meeting.getId(),
                            meeting.getGroup().getId(),
                            meeting.getTitle(),
                            meeting.getMeetingLink(),
                            meeting.getStatus()
                    );
                })
                .toList();
    }
}
