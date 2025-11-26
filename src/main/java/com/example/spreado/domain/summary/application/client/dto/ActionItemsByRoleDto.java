package com.example.spreado.domain.summary.application.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 역할별 액션 아이템을 담는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActionItemsByRoleDto {

    @JsonProperty("PM")
    private List<String> pm;

    @JsonProperty("PD")
    private List<String> pd;

    @JsonProperty("FE")
    private List<String> fe;

    @JsonProperty("BE")
    private List<String> be;

    @JsonProperty("AI")
    private List<String> ai;

    @JsonProperty("ALL")
    private List<String> all;
}
