package com.example.spreado.domain.summary.application.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마일스톤 정보를 담는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneDto {

    @JsonProperty("task")
    private String task;

    @JsonProperty("deadline")
    private String deadline;
}
