package com.example.spreado.domain.meeting.application;

import com.example.spreado.domain.group.core.entity.Group;
import com.example.spreado.domain.group.core.repository.GroupMemberRepository;
import com.example.spreado.domain.group.core.repository.GroupRepository;
import com.example.spreado.domain.liveblocks.application.LiveblocksService;
import com.example.spreado.domain.meeting.api.dto.request.MeetingCreateRequest;
import com.example.spreado.domain.meeting.api.dto.response.*;
import com.example.spreado.domain.meeting.core.entity.Meeting;
import com.example.spreado.domain.meeting.core.entity.MeetingJoin;
import com.example.spreado.domain.meeting.core.entity.MeetingStatus;
import com.example.spreado.domain.meeting.core.util.RoomIdPolicy;
import com.example.spreado.domain.note.core.entity.Note;
import com.example.spreado.domain.meeting.core.repository.MeetingJoinRepository;
import com.example.spreado.domain.meeting.core.repository.MeetingRepository;
import com.example.spreado.domain.note.core.service.NoteService;
import com.example.spreado.domain.note.core.repository.NoteRepository;
import com.example.spreado.domain.note.api.dto.response.NoteResponse;
import com.example.spreado.domain.user.core.entity.User;
import com.example.spreado.domain.user.core.repository.UserRepository;
import com.example.spreado.global.shared.exception.BadRequestException;
import com.example.spreado.global.shared.exception.ForbiddenException;
import com.example.spreado.global.shared.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingJoinRepository meetingJoinRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final LiveblocksService liveblocksService;
    private final NoteService noteService;
    private final NoteRepository noteRepository;
    private final RoomIdPolicy roomIdPolicy;
    private final ObjectMapper objectMapper;
    private final EntityManager em;

    public MeetingCreateResponse createMeeting(@Valid MeetingCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        Meeting meeting = Meeting.create(group, user, request.title());
        meetingRepository.save(meeting);

        MeetingJoin hostJoin = MeetingJoin.create(meeting, user);
        meetingJoinRepository.save(hostJoin);
        em.flush();

        liveblocksService.createRoomForMeeting(meeting.getId());

        Map<String, Object> tokenJson = liveblocksService.getToken(meeting.getId(), userId);

        return new MeetingCreateResponse(meeting.getId(), tokenJson.get("token").toString());
    }

    public MeetingJoinResponse joinMeeting(Long meetingId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        Group group = meeting.getGroup();

        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new ForbiddenException("해당 그룹의 멤버가 아니므로 회의에 참여할 수 없습니다.");
        }

        // 이미 참여 중이 아니면 새로 참여 처리
        if (!meetingJoinRepository.existsByMeetingIdAndUserId(meeting.getId(), userId)) {
            MeetingJoin meetingJoin = MeetingJoin.create(meeting, user);
            meetingJoinRepository.save(meetingJoin);
            em.flush();
        }

        // 참여 여부와 관계없이 토큰 발급
        Map<String, Object> tokenJson = liveblocksService.getToken(meeting.getId(), userId);

        return new MeetingJoinResponse(meeting.getId(), userId, tokenJson.get("token").toString());
    }

    public void leaveMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        MeetingJoin membership = meetingJoinRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new NotFoundException("해당 회의 참여 정보를 찾을 수 없습니다."));

        if (membership.getMeeting().getCreator().getId().equals(userId)) {
            throw new BadRequestException("호스트는 회의에서 나갈 수 없습니다.");
        }

        meetingJoinRepository.deleteByMeetingIdAndUserId(meetingId, userId);
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
                        meeting.getCreatedAt(),
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

        Long noteId = noteRepository.findByMeetingId(meetingId)
                .map(Note::getId)
                .orElse(null);

        return new MeetingDetailResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getStatus(),
                participantResponses,
                noteId
        );
    }

    public NoteResponse endMeeting(Long meetingId, Long userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        if (!meeting.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("호스트만 회의를 종료할 수 있습니다.");
        }

        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new BadRequestException("이미 종료된 회의입니다.");
        }

        meeting.endMeeting();

        String roomId = roomIdPolicy.toRoomId(meeting);

        JsonNode noteContent = liveblocksService.fetchStorageJson(roomId);
        JsonNode wrappedContent = wrap(noteContent);

        liveblocksService.deleteRoomForMeeting(meeting.getId());

        Note note = Note.create(meeting, wrappedContent);
        noteService.save(note);

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
                            meeting.getCreatedAt(),
                            meeting.getStatus()
                    );
                })
                .toList();
    }

    public List<MeetingSummaryResponse> getOngoingMeetings(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        List<Meeting> meetings = meetingRepository.findAllByGroupIdAndStatus(group.getId(), MeetingStatus.ONGOING);

        return meetings.stream()
                .map(meeting -> new MeetingSummaryResponse(
                        meeting.getId(),
                        group.getId(),
                        meeting.getTitle(),
                        meeting.getCreatedAt(),
                        meeting.getStatus()
                ))
                .toList();
    }

    private JsonNode wrap(JsonNode content) {
        ObjectNode wrapper = objectMapper.createObjectNode();
        wrapper.set("data", content);
        return wrapper;
    }
}
