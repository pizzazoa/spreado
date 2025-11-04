package com.example.spreado.domain.group.application;

import com.example.spreado.domain.group.api.dto.request.GroupCreateRequest;
import com.example.spreado.domain.group.api.dto.request.GroupJoinRequest;
import com.example.spreado.domain.group.api.dto.response.GroupCreateResponse;
import com.example.spreado.domain.group.api.dto.response.GroupDetailResponse;
import com.example.spreado.domain.group.api.dto.response.GroupJoinResponse;
import com.example.spreado.domain.group.api.dto.response.GroupMemberResponse;
import com.example.spreado.domain.group.api.dto.response.GroupSummaryResponse;
import com.example.spreado.domain.group.core.entity.Group;
import com.example.spreado.domain.group.core.entity.GroupMember;
import com.example.spreado.domain.group.core.entity.GroupRole;
import com.example.spreado.domain.group.core.repository.GroupMemberRepository;
import com.example.spreado.domain.group.core.repository.GroupRepository;
import com.example.spreado.domain.group.core.service.GroupInviteLinkService;
import com.example.spreado.domain.user.core.entity.User;
import com.example.spreado.domain.user.core.repository.UserRepository;
import com.example.spreado.global.shared.exception.BadRequestException;
import com.example.spreado.global.shared.exception.ForbiddenException;
import com.example.spreado.global.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupInviteLinkService groupInviteLinkService;

    @Transactional
    public GroupCreateResponse createGroup(GroupCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        String inviteLink = groupInviteLinkService.generateUniqueInviteLink();
        Group group = Group.create(request.name(), inviteLink);
        groupRepository.save(group);

        GroupRole role = GroupRole.from(request.role());
        GroupMember member = GroupMember.create(group, user, role);
        groupMemberRepository.save(member);

        return new GroupCreateResponse(group.getId(), group.getInviteLink());
    }

    public List<GroupSummaryResponse> getMyGroups(Long userId) {
        List<GroupMember> memberships = groupMemberRepository.findAllByUserId(userId);

        return memberships.stream()
                .map(member -> new GroupSummaryResponse(
                        member.getGroup().getId(),
                        member.getGroup().getName(),
                        member.getRole().name()
                ))
                .toList();
    }

    public GroupDetailResponse getGroupDetail(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        GroupMember myMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("그룹에 참여 중인 사용자만 접근할 수 있습니다."));

        List<GroupMember> members = groupMemberRepository.findAllByGroupId(groupId);

        List<GroupMemberResponse> memberResponses = members.stream()
                .map(member -> new GroupMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getName(),
                        member.getRole().name()
                ))
                .toList();

        return new GroupDetailResponse(
                group.getId(),
                group.getName(),
                group.getInviteLink(),
                myMembership.getRole().name(),
                memberResponses
        );
    }

    @Transactional
    public GroupJoinResponse joinGroup(GroupJoinRequest request, Long userId) {
        Group group = groupRepository.findByInviteLink(request.inviteLink())
                .orElseThrow(() -> new NotFoundException("해당 초대 링크로 그룹을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
            throw new BadRequestException("이미 그룹에 참여한 사용자입니다.");
        }

        GroupRole role = GroupRole.from(request.role());
        GroupMember member = GroupMember.create(group, user, role);
        groupMemberRepository.save(member);

        return new GroupJoinResponse(
                member.getGroup().getId(),
                member.getUser().getId(),
                member.getRole().name()
        );
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember membership = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BadRequestException("그룹에 참여하지 않은 사용자입니다."));

        groupMemberRepository.deleteById(membership.getId());
    }
}
