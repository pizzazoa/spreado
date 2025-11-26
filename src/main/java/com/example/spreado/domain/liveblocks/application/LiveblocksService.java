package com.example.spreado.domain.liveblocks.application;

import com.example.spreado.domain.group.core.entity.GroupMember;
import com.example.spreado.domain.group.core.repository.GroupMemberRepository;
import com.example.spreado.domain.meeting.core.entity.Meeting;
import com.example.spreado.domain.meeting.core.entity.MeetingStatus;
import com.example.spreado.domain.meeting.core.repository.MeetingJoinRepository;
import com.example.spreado.domain.meeting.core.repository.MeetingRepository;
import com.example.spreado.domain.meeting.core.util.RoomIdPolicy;
import com.example.spreado.domain.user.core.entity.User;
import com.example.spreado.domain.user.core.repository.UserRepository;
import com.example.spreado.global.shared.exception.ForbiddenException;
import com.example.spreado.global.shared.exception.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveblocksService {

    private final WebClient defaultWebClient;
    private final MeetingRepository meetingRepository;
    private final RoomIdPolicy roomIdPolicy;
    private final MeetingJoinRepository meetingJoinRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${LIVEBLOCKS_API_BASE}")
    private String apiBaseUrl;

    @Value("${LIVEBLOCKS_SECRET}")
    private String secret;

    public Map<String, Object> getToken(Long meetingId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다."));

        if (meeting.getStatus() == MeetingStatus.ENDED) {
            throw new ForbiddenException("진행 중인 회의에만 접근할 수 있습니다.");
        }

        GroupMember myMembership = groupMemberRepository.findByGroupIdAndUserId(meeting.getGroup().getId(), userId)
                .orElseThrow(() -> new ForbiddenException("그룹에 참여 중인 사용자만 접근할 수 있습니다."));

        if (!meetingJoinRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
            throw new ForbiddenException("해당 회의에 참여 중인 사용자가 아닙니다.");
        }

        String roomId = roomIdPolicy.toRoomId(meeting);

        Map<String, Object> userInfo = Map.of(
                "id", "User#" + userId,
                "name", user.getName(),
                "role", myMembership.getRole().name()
        );

        Map<String, Object> tokenJson = this.issueClientToken(
                roomId,
                String.valueOf(userId),
                userInfo
        );

        return tokenJson;
    }

    public JsonNode fetchStorageJson(String roomId) {
        String json = this.fetchStorageJsonRaw(roomId);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Liveblocks storage JSON (roomId="
                    + roomId + "): " + e.getMessage());
        }
    }

    private Map<String, Object> issueClientToken(String roomId, String userId, Map<String, Object> userInfo) {
        String url = apiBaseUrl + "/v2/authorize-user";

        Map<String, Object> payload = new HashMap<>();
        if (userId != null) payload.put("userId", userId);
        if (userInfo != null && !userInfo.isEmpty()) payload.put("userInfo", userInfo);

        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put(roomId, List.of("room:write"));

        payload.put("permissions", permissions);

        return defaultWebClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secret)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    private String fetchStorageJsonRaw(String roomId) {
        String url = apiBaseUrl + "/v2/rooms/" + roomId + "/storage?format=json";
        return defaultWebClient.get()
                .uri(URI.create(url))   // String 대신 URI.create 사용 권장
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secret)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
