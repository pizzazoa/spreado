package com.example.spreado.domain.note.core.service;

import com.example.spreado.domain.meeting.core.entity.Meeting;
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

    /**
     * 현재는 테스트용 더미 노트를 생성
     * 추후 구현 예정
     */
    public Note generateNoteForMeeting(Meeting meeting) {
        String content = "Test Notes for meeting: " + meeting.getTitle();

        Note note = Note.createFromText(meeting, content);
        noteRepository.save(note);

        return note;
    }

    public NoteResponse getNoteDetail(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("해당 노트를 찾을 수 없습니다."));

        return new NoteResponse(note.getId(), note.getMeeting().getId(), note.getContent());
    }
}
