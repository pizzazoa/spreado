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
        log.info("요약 생성 시작 - noteId: {}", noteId);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("해당 노트를 찾을 수 없습니다."));
        log.debug("노트 조회 완료 - noteId: {}, contentLength: {}", noteId, note.getContent().toString().length());

        String plainText = documentPreprocessor.toPlainText(note.getContent());
        log.debug("텍스트 전처리 완료 - plainTextLength: {}", plainText.length());

        if (!StringUtils.hasText(plainText)) {
            log.warn("요약할 회의록 내용이 비어 있음 - noteId: {}", noteId);
            throw new BadRequestException("요약할 회의록 내용이 비어 있습니다.");
        }

        String prompt = promptService.buildSummaryPrompt(plainText);
        log.debug("프롬프트 생성 완료 - promptLength: {}", prompt.length());

        try {
            log.info("AI 요약 요청 시작 - noteId: {}", noteId);
            var summaryDto = aiClient.requestSummary(prompt);
            log.info("AI 요약 요청 완료 - noteId: {}", noteId);

            String summaryJson = convertToJson(summaryDto);
            log.debug("요약 JSON 변환 완료 - jsonLength: {}", summaryJson.length());

            return summaryRepository.findByNoteId(noteId)
                    .map(existing -> {
                        log.info("기존 요약 업데이트 - summaryId: {}", existing.getId());
                        existing.updateSummaryJson(summaryJson);
                        return SummaryResponse.from(existing);
                    })
                    .orElseGet(() -> {
                        log.info("새로운 요약 생성 - noteId: {}", noteId);
                        return SummaryResponse.from(summaryRepository.save(Summary.create(note, summaryJson)));
                    });
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
