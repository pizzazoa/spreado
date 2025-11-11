package com.example.spreado.domain.note.core.service;

import com.example.spreado.domain.note.api.dto.response.NoteResponse;
import com.example.spreado.domain.note.core.entity.Note;
import com.example.spreado.domain.note.core.repository.NoteRepository;
import com.example.spreado.global.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;

    public Note save(Note note) {
        return noteRepository.save(note);
    }

    public NoteResponse getNoteDetail(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("해당 노트를 찾을 수 없습니다."));

        return new NoteResponse(note.getId(), note.getMeeting().getId(), note.getContent());
    }
}
