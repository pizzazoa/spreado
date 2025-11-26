package com.example.spreado.domain.summary.core.repository;

import com.example.spreado.domain.summary.core.entity.Summary;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SummaryRepository {

    private final EntityManager entityManager;

    public Summary save(Summary summary) {
        if (summary.getId() == null) {
            entityManager.persist(summary);
            return summary;
        }
        return entityManager.merge(summary);
    }

    public Optional<Summary> findById(Long id) {
        return entityManager.createQuery("""
                        SELECT s FROM Summary s
                        JOIN FETCH s.note
                        WHERE s.id = :id
                        """, Summary.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public Optional<Summary> findByNoteId(Long noteId) {
        return entityManager.createQuery("""
                        SELECT s FROM Summary s
                        JOIN FETCH s.note n
                        WHERE n.id = :noteId
                        """, Summary.class)
                .setParameter("noteId", noteId)
                .getResultStream()
                .findFirst();
    }

    public void delete(Summary summary) {
        entityManager.remove(summary);
    }
}
