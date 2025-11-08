package com.example.spreado.domain.meeting.core.repository;

import com.example.spreado.domain.meeting.core.entity.MeetingJoin;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingJoinRepository {

    private final EntityManager em;

    public void save(MeetingJoin meetingJoin) {
        em.persist(meetingJoin);
    }

    public Optional<MeetingJoin> findByMeetingIdAndUserId(Long meetingId, Long userId) {
        return em.createQuery("""
                        SELECT mj FROM MeetingJoin mj
                        JOIN FETCH mj.meeting m
                        JOIN FETCH mj.user u
                        WHERE m.id = :meetingId AND u.id = :userId
                        """, MeetingJoin.class)
                .setParameter("meetingId", meetingId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst();
    }

    public void deleteById(Long meetingJoinId) {
        MeetingJoin reference = em.getReference(MeetingJoin.class, meetingJoinId);
        em.remove(reference);
    }

    public boolean existsByMeetingIdAndUserId(Long meetingId, Long userId) {
        Long count = em.createQuery("""
                        SELECT COUNT(mj) FROM MeetingJoin mj
                        WHERE mj.meeting.id = :meetingId AND mj.user.id = :userId
                        """, Long.class)
                .setParameter("meetingId", meetingId)
                .setParameter("userId", userId)
                .getSingleResult();
        return count > 0;
    }

    public List<MeetingJoin> findAllByMeetingId(Long meetingId) {
        return em.createQuery("""
                        SELECT mj FROM MeetingJoin mj
                        JOIN FETCH mj.user u
                        WHERE mj.meeting.id = :meetingId
                        """, MeetingJoin.class)
                .setParameter("meetingId", meetingId)
                .getResultList();
    }

    public List<MeetingJoin> findAllByUserId(Long userId) {
        return em.createQuery("""
                        SELECT mj FROM MeetingJoin mj
                        JOIN FETCH mj.meeting m
                        WHERE mj.user.id = :userId
                        """, MeetingJoin.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
