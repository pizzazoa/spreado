package com.example.spreado.domain.summary.application.client;

import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseFormatTextConfig;
import com.openai.models.responses.ResponseFormatTextJsonSchemaConfig;
import com.openai.models.responses.ResponseTextConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SummaryResponseFormatFactory {

    private static final String SCHEMA_NAME = "meeting_summary";
    private static final List<String> REQUIRED_FIELDS = List.of("summary", "milestones", "actionItemsByRole");
    private static final List<String> ROLE_TYPES = List.of("PM", "PD", "FE", "BE", "AI", "ALL");

    private final ResponseTextConfig cachedFormat = buildResponseFormat();

    public ResponseTextConfig getResponseTextConfig() {
        return cachedFormat;
    }

    private ResponseTextConfig buildResponseFormat() {
        Map<String, Object> rootSchema = buildRootSchema();

        ResponseFormatTextJsonSchemaConfig jsonSchema = ResponseFormatTextJsonSchemaConfig.builder()
                .name(SCHEMA_NAME)
                .schema(ResponseFormatTextJsonSchemaConfig.Schema.builder()
                        .additionalProperties(toJsonValueMap(rootSchema))
                        .build())
                .strict(true)
                .build();

        return ResponseTextConfig.builder()
                .format(ResponseFormatTextConfig.ofJsonSchema(jsonSchema))
                .build();
    }

    private Map<String, Object> buildRootSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", REQUIRED_FIELDS,
                "properties", Map.of(
                        "summary", Map.of("type", "string"),
                        "milestones", buildMilestoneSchema(),
                        "actionItemsByRole", buildActionItemsSchema()
                )
        );
    }

    private Map<String, Object> buildMilestoneSchema() {
        return Map.of(
                "type", "array",
                "default", List.of(),
                "items", Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "required", List.of("task", "deadline"),
                        "properties", Map.of(
                                "task", Map.of("type", "string"),
                                "deadline", Map.of("type", "string")
                        )
                )
        );
    }

    private Map<String, Object> buildActionItemsSchema() {
        Map<String, Map<String, Object>> roleProperties = ROLE_TYPES.stream()
                .collect(Collectors.toMap(
                        role -> role,
                        role -> Map.of(
                                "type", "array",
                                "items", Map.of("type", "string"),
                                "default", List.of()
                        )
                ));

        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", roleProperties,
                "required", ROLE_TYPES
        );
    }

    private Map<String, JsonValue> toJsonValueMap(Map<String, Object> source) {
        return source.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> JsonValue.from(entry.getValue())
                ));
    }
}
