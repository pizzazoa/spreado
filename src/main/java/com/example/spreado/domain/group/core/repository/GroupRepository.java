package com.example.spreado.domain.group.core.repository;

import com.example.spreado.domain.group.core.entity.Group;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupRepository {

    private final EntityManager em;

    public Group save(Group group) {
        em.persist(group);
        return group;
    }

    public Optional<Group> findById(Long id) {
        return em.createQuery("SELECT g FROM Group g WHERE g.id = :id", Group.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public Optional<Group> findByInviteLink(String inviteLink) {
        return em.createQuery("SELECT g FROM Group g WHERE g.inviteLink = :inviteLink", Group.class)
                .setParameter("inviteLink", inviteLink)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByInviteLink(String inviteLink) {
        Long count = em.createQuery("SELECT COUNT(g) FROM Group g WHERE g.inviteLink = :inviteLink", Long.class)
                .setParameter("inviteLink", inviteLink)
                .getSingleResult();
        return count > 0;
    }
}
