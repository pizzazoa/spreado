package com.example.spreado.domain.summary.application.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI Responses API의 structured output 응답을 받기 위한 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingSummaryDto {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("milestones")
    private List<MilestoneDto> milestones;

    @JsonProperty("actionItemsByRole")
    private ActionItemsByRoleDto actionItemsByRole;
}
