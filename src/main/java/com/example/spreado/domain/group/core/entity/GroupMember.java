package com.example.spreado.domain.group.core.entity;

import com.example.spreado.domain.user.core.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private GroupRole role;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    public static GroupMember create(Group group, User user, GroupRole role) {
        GroupMember member = new GroupMember();
        member.group = group;
        member.user = user;
        member.role = role;
        member.joinedAt = OffsetDateTime.now();
        return member;
    }
}
