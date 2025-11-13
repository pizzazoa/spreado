package com.example.spreado.domain.group.api;

import com.example.spreado.domain.group.api.dto.request.GroupCreateRequest;
import com.example.spreado.domain.group.api.dto.request.GroupJoinRequest;
import com.example.spreado.domain.group.api.dto.response.GroupCreateResponse;
import com.example.spreado.domain.group.api.dto.response.GroupDetailResponse;
import com.example.spreado.domain.group.api.dto.response.GroupJoinResponse;
import com.example.spreado.domain.group.api.dto.response.GroupSummaryResponse;
import com.example.spreado.domain.group.application.GroupService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Group API", description = "그룹 생성 및 참여 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "그룹 생성",
            description = "그룹을 생성하고 초대 링크를 발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "그룹 생성 성공", content = @Content(schema = @Schema(implementation = GroupCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    public GroupCreateResponse createGroup(@Valid @RequestBody GroupCreateRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return groupService.createGroup(request, userId);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "내 그룹 목록 조회",
            description = "현재 참여 중인 그룹 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GroupSummaryResponse.class)))
    public List<GroupSummaryResponse> getMyGroups(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return groupService.getMyGroups(userId);
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "그룹 상세 조회",
            description = "특정 그룹의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GroupDetailResponse.class))),
            @ApiResponse(responseCode = "403", description = "그룹 미참여", content = @Content),
            @ApiResponse(responseCode = "404", description = "그룹 없음", content = @Content)
    })
    public GroupDetailResponse getGroupDetail(@PathVariable Long groupId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return groupService.getGroupDetail(groupId, userId);
    }

    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "그룹 참여",
            description = "초대 링크를 통해 그룹에 참여합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 참여 성공", content = @Content(schema = @Schema(implementation = GroupJoinResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음", content = @Content)
    })
    public GroupJoinResponse joinGroup(@Valid @RequestBody GroupJoinRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return groupService.joinGroup(request, userId);
    }

    @PostMapping("/{groupId}/leave")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "그룹 나가기",
            description = "참여 중인 그룹에서 나갑니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "나가기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
    })
    public void leaveGroup(@PathVariable Long groupId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        groupService.leaveGroup(groupId, userId);
    }

    @DeleteMapping("/{groupId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "그룹 삭제",
            description = "그룹을 삭제합니다. 그룹 리더만 삭제할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음", content = @Content)
    })
    public void deleteGroup(@PathVariable Long groupId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        groupService.deleteGroup(groupId, userId);
    }
}
