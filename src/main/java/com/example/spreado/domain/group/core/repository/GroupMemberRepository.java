package com.example.spreado.domain.group.core.repository;

import com.example.spreado.domain.group.core.entity.GroupMember;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepository {

    private final EntityManager em;

    public GroupMember save(GroupMember member) {
        em.persist(member);
        return member;
    }

    public boolean existsByGroupIdAndUserId(Long groupId, Long userId) {
        Long count = em.createQuery("""
                        SELECT COUNT(gm) FROM GroupMember gm
                        WHERE gm.group.id = :groupId AND gm.user.id = :userId
                        """, Long.class)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }

    public Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId) {
        return em.createQuery("""
                        SELECT gm FROM GroupMember gm
                        JOIN FETCH gm.group
                        JOIN FETCH gm.user
                        WHERE gm.group.id = :groupId AND gm.user.id = :userId
                        """, GroupMember.class)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    public List<GroupMember> findAllByUserId(Long userId) {
        return em.createQuery("""
                        SELECT gm FROM GroupMember gm
                        JOIN FETCH gm.group
                        JOIN FETCH gm.user
                        WHERE gm.user.id = :userId
                        """, GroupMember.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<GroupMember> findAllByGroupId(Long groupId) {
        return em.createQuery("""
                        SELECT gm FROM GroupMember gm
                        JOIN FETCH gm.group
                        JOIN FETCH gm.user
                        WHERE gm.group.id = :groupId
                        """, GroupMember.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    public void deleteById(Long groupMemberId) {
        GroupMember reference = em.getReference(GroupMember.class, groupMemberId);
        em.remove(reference);
    }

}
