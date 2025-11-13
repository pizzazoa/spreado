package com.example.spreado.domain.meeting.core.repository;

import com.example.spreado.domain.meeting.core.entity.Meeting;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MeetingRepository {

    private final EntityManager em;

    public Meeting save(Meeting meeting) {
        em.persist(meeting);
        return meeting;
    }

    public Optional<Meeting> findById(Long id) {
        return em.createQuery("SELECT m FROM Meeting m WHERE m.id = :id", Meeting.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public List<Meeting> findAllByGroupId(Long id) {
        return em.createQuery("SELECT m FROM Meeting m WHERE m.group.id = :id", Meeting.class)
                .setParameter("id", id)
                .getResultList();
    }

    public Optional<Meeting> findByNoteId(Long noteId) {
        return em.createQuery("SELECT m FROM Meeting m JOIN Note n ON n.meeting = m WHERE n.id = :noteId", Meeting.class)
                .setParameter("noteId", noteId)
                .getResultStream()
                .findFirst();
    }

    public List<String> findParticipantEmailsByMeetingId(Long meetingId) {
        return em.createQuery("select mj.user.email " +
                "from MeetingJoin mj " +
                "where mj.meeting.id = :meetingId", String.class)
                .setParameter("meetingId", meetingId)
                .getResultList();
    }
}
