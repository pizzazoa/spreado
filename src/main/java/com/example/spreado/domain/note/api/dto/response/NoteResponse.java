package com.example.spreado.domain.note.api.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record NoteResponse(
        Long noteId,
        Long meetingId,
        JsonNode content
) {
}
