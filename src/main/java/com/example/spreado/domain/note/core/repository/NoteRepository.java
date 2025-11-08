package com.example.spreado.domain.note.core.repository;

import com.example.spreado.domain.note.core.entity.Note;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NoteRepository {

    private final EntityManager em;

    public Note save(Note note) {
        em.persist(note);
        return note;
    }

    public Optional<Note> findById(Long noteId) {
        return em.createQuery("SELECT n FROM Note n WHERE n.id = :noteId", Note.class)
                .setParameter("noteId", noteId)
                .getResultStream()
                .findFirst();
    }
}
