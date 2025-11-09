package com.example.spreado.domain.summary.application;

import com.example.spreado.domain.note.core.entity.Note;
import com.example.spreado.domain.note.core.repository.NoteRepository;
import com.example.spreado.domain.summary.application.client.AiClient;
import com.example.spreado.domain.summary.core.repository.SummaryRepository;
import com.example.spreado.domain.summary.api.dto.response.SummaryResponse;
import com.example.spreado.domain.summary.api.dto.request.SummaryUpdateRequest;
import com.example.spreado.domain.summary.core.entity.Summary;
import com.example.spreado.global.shared.exception.BadRequestException;
import com.example.spreado.global.shared.exception.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SummaryService {

    private final NoteRepository noteRepository;
    private final SummaryRepository summaryRepository;
    private final SummaryDocumentPreprocessor documentPreprocessor;
    private final PromptService promptService;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public SummaryResponse generateSummary(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("해당 노트를 찾을 수 없습니다."));

        String plainText = documentPreprocessor.toPlainText(note.getContent());
        if (!StringUtils.hasText(plainText)) {
            throw new BadRequestException("요약할 회의록 내용이 비어 있습니다.");
        }

        String prompt = promptService.buildSummaryPrompt(plainText);
        String rawResponse = aiClient.requestSummary(prompt);
        String normalizedJson = normalizeResponse(rawResponse);

        return summaryRepository.findByNoteId(noteId)
                .map(existing -> {
                    existing.updateSummaryJson(normalizedJson);
                    return SummaryResponse.from(existing);
                })
                .orElseGet(() -> SummaryResponse.from(summaryRepository.save(Summary.create(note, normalizedJson))));
    }

    public SummaryResponse getSummary(Long noteId) {
        Summary summary = summaryRepository.findByNoteId(noteId)
                .orElseThrow(() -> new NotFoundException("해당 노트의 요약이 존재하지 않습니다."));

        return SummaryResponse.from(summary);
    }

    @Transactional
    public SummaryResponse updateSummary(Long summaryId, SummaryUpdateRequest request) {
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new NotFoundException("해당 요약을 찾을 수 없습니다."));

        summary.updateSummaryJson(request.summaryJson());
        return SummaryResponse.from(summary);
    }

    @Transactional
    public void deleteSummary(Long summaryId) {
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new NotFoundException("해당 요약을 찾을 수 없습니다."));

        summaryRepository.delete(summary);
    }


    // 헬퍼 메서드
    private String normalizeResponse(String response) {
        if (!StringUtils.hasText(response)) {
            throw new BadRequestException("AI 응답이 비어 있습니다.");
        }

        String trimmed = response.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(trimmed);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("AI 응답이 JSON 형식이 아닙니다.");
        }
    }
}
