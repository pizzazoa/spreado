package com.example.spreado.domain.group.core.service;

import com.example.spreado.domain.group.core.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupInviteLinkService {

    private static final int MAX_INVITE_GENERATION_ATTEMPTS = 10;

    private final GroupRepository groupRepository;

    @Value("${group.invite-link.prefix}")
    private String inviteLinkPrefix;

    public String generateUniqueInviteLink() {
        for (int attempt = 0; attempt < MAX_INVITE_GENERATION_ATTEMPTS; attempt++) {
            String candidate = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(UUID.randomUUID().toString().getBytes())
                    .substring(0, 12);
            String inviteLink = buildInviteLink(candidate);
            if (!groupRepository.existsByInviteLink(inviteLink)) {
                return inviteLink;
            }
        }

        throw new IllegalStateException("고유한 초대 링크를 생성하지 못했습니다.");
    }

    private String buildInviteLink(String candidate) {
        if (inviteLinkPrefix == null || inviteLinkPrefix.isBlank()) {
            throw new IllegalStateException("초대 링크 prefix 설정이 존재하지 않습니다.");
        }
        String normalizedPrefix = inviteLinkPrefix.endsWith("/")
                ? inviteLinkPrefix.substring(0, inviteLinkPrefix.length() - 1)
                : inviteLinkPrefix;
        return normalizedPrefix + "/" + candidate;
    }
}
