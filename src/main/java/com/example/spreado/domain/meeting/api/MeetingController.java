package com.example.spreado.domain.meeting.api;

import com.example.spreado.domain.group.api.dto.response.GroupJoinResponse;
import com.example.spreado.domain.meeting.api.dto.request.MeetingCreateRequest;
import com.example.spreado.domain.meeting.api.dto.response.*;
import com.example.spreado.domain.meeting.application.MeetingService;
import com.example.spreado.domain.note.api.dto.response.NoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@Tag(name = "Meeting API", description = "회의 생성 및 참여 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/meeting")
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping(params = "groupId")
    @Operation(
            summary = "그룹의 회의 목록 조회",
            description = "특정 그룹에 속한 회의 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MeetingSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음", content = @Content)
    })
    public List<MeetingSummaryResponse> getMeetingsByGroup(@RequestParam Long groupId) {
        return meetingService.getMeetingsByGroup(groupId);
    }

    @GetMapping("/{meetingId}")
    @Operation(
            summary = "회의 상세 조회",
            description = "특정 회의의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MeetingDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음", content = @Content)
    })
    public MeetingDetailResponse getMeetingDetail(@PathVariable Long meetingId) {
        return meetingService.getMeetingDetail(meetingId);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "회의 생성",
            description = "회의를 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회의 생성 성공", content = @Content(schema = @Schema(implementation = MeetingCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    public MeetingCreateResponse createMeeting(@Valid @RequestBody MeetingCreateRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return meetingService.createMeeting(request, userId);
    }

    @PostMapping("/{meetingId}/join")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "회의 참여",
            description = "회의에 참여합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회의 참여 성공", content = @Content(schema = @Schema(implementation = GroupJoinResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음", content = @Content)
    })
    public MeetingJoinResponse joinMeeting(@PathVariable Long meetingId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return meetingService.joinMeeting(meetingId, userId);
    }

    @PostMapping("/{meetingId}/leave")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(NO_CONTENT)
    @Operation(
            summary = "회의 나가기",
            description = "참여 중인 회의에서 나갑니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "나가기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    public void leaveMeeting(@PathVariable Long meetingId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        meetingService.leaveMeeting(meetingId, userId);
    }

    @PostMapping("/{meetingId}/end")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "회의 종료",
            description = "회의를 종료합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회의 종료 성공", content = @Content(schema = @Schema(implementation = NoteResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음", content = @Content)
    })
    public NoteResponse endMeeting(@PathVariable Long meetingId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return meetingService.endMeeting(meetingId, userId);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "내 회의 목록 조회",
            description = "현재 참여 중인 회의 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MeetingMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    public List<MeetingSummaryResponse> getMyMeetings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return meetingService.getMyMeetings(userId);
    }
}
