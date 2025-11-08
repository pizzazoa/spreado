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

    public void setMeetingLink(Long meetingId, String meetingLink) {
        em.createQuery("UPDATE Meeting m SET m.meetingLink = :meetingLink WHERE m.id = :meetingId")
                .setParameter("meetingLink", meetingLink)
                .setParameter("meetingId", meetingId)
                .executeUpdate();
    }
}
