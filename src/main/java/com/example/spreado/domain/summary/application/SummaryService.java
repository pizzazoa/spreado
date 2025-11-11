package com.example.spreado.domain.summary.application;

import com.example.spreado.domain.meeting.core.repository.MeetingJoinRepository;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SummaryService {

    private final NoteRepository noteRepository;
    private final SummaryRepository summaryRepository;
    private final MeetingJoinRepository meetingJoinRepository;
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

        try {
            var summaryDto = aiClient.requestSummary(prompt);
            String summaryJson = convertToJson(summaryDto);

            return summaryRepository.findByNoteId(noteId)
                    .map(existing -> {
                        existing.updateSummaryJson(summaryJson);
                        return SummaryResponse.from(existing);
                    })
                    .orElseGet(() -> SummaryResponse.from(summaryRepository.save(Summary.create(note, summaryJson))));
        } catch (Exception e) {
            log.error("요약 생성 중 오류 발생 - noteId: {}, errorType: {}, errorMessage: {}",
                    noteId, e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
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
    private String convertToJson(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("요약 데이터 JSON 변환 실패 - dtoType: {}, errorMessage: {}",
                    dto.getClass().getName(), e.getMessage(), e);
            throw new IllegalStateException("요약 데이터를 JSON으로 변환하는 중 오류가 발생했습니다.", e);
        }
    }
}
