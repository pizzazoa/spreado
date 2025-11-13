package com.example.spreado.domain.summary.api.dto;

import com.example.spreado.domain.summary.api.dto.request.SummaryUpdateRequest;
import com.example.spreado.domain.summary.api.dto.response.SummaryResponse;
import com.example.spreado.domain.summary.application.SummaryService;
import com.example.spreado.domain.summary.core.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Summary API", description = "회의록 요약 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/summaries")
public class SummaryController {

    private final SummaryService summaryService;
    private final MailService mailService;

    @PostMapping("/{noteId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "회의록 요약 생성",
            description = "노트의 content JSON을 전처리하고 AI로부터 요약을 생성합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "요약 생성 성공", content = @Content(schema = @Schema(implementation = SummaryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "노트를 찾을 수 없음", content = @Content)
            }
    )
    public SummaryResponse generateSummary(@PathVariable Long noteId) {
        return summaryService.generateSummary(noteId);
    }

    @GetMapping("/{noteId}")
    @Operation(
            summary = "회의록 요약 조회",
            description = "노트 ID로 저장된 요약을 조회합니다.",
            responses = @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = SummaryResponse.class)))
    )
    public SummaryResponse getSummary(@PathVariable Long noteId) {
        return summaryService.getSummary(noteId);
    }

    @PutMapping("/{summaryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "회의록 요약 수정",
            description = "AI가 생성한 요약을 수동으로 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = SummaryResponse.class))),
                    @ApiResponse(responseCode = "404", description = "요약을 찾을 수 없음", content = @Content)
            }
    )
    public SummaryResponse updateSummary(@PathVariable Long summaryId, @Valid @RequestBody SummaryUpdateRequest request) {
        return summaryService.updateSummary(summaryId, request);
    }

    @DeleteMapping("/{summaryId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "회의록 요약 삭제",
            description = "저장된 요약을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public void deleteSummary(@PathVariable Long summaryId) {
        summaryService.deleteSummary(summaryId);
    }

    @PostMapping("/{summaryId}/mail")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "회의록 요약 이메일 전송",
            description = """
                    저장된 요약을 이메일로 전송합니다.
                    회의에 참여한 참여자 모두에게 전송됩니다.
                    회의 주최자 한 명만 사용하면 됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "이메일 전송 성공"),
                    @ApiResponse(responseCode = "404", description = "요약을 찾을 수 없음", content = @Content)
            }
    )
    public void sendSummaryByEmail(@PathVariable Long summaryId) {
        mailService.sendMail(summaryId);
    }
}
