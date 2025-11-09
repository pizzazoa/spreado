package com.example.spreado.domain.summary.application.client;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SummaryResponseFormatFactory {

    private final Map<String, Object> cachedFormat = Collections.unmodifiableMap(buildResponseFormat());

    public Map<String, Object> getResponseFormat() {
        return cachedFormat;
    }

    private Map<String, Object> buildResponseFormat() {
        Map<String, Object> format = new HashMap<>();
        format.put("type", "json_schema");

        Map<String, Object> jsonSchema = new HashMap<>();
        jsonSchema.put("name", "meeting_summary");
        jsonSchema.put("schema", buildRootSchema());
        format.put("json_schema", jsonSchema);

        return format;
    }

    private Map<String, Object> buildRootSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("required", List.of("summary", "milestones", "actionItemsByRole"));

        Map<String, Object> properties = new HashMap<>();
        properties.put("summary", Map.of("type", "string"));
        properties.put("milestones", buildMilestoneSchema());
        properties.put("actionItemsByRole", buildActionItemsSchema());

        schema.put("properties", properties);
        return schema;
    }

    private Map<String, Object> buildMilestoneSchema() {
        Map<String, Object> milestoneSchema = new HashMap<>();
        milestoneSchema.put("type", "array");
        milestoneSchema.put("default", List.of());

        Map<String, Object> milestoneItem = new HashMap<>();
        milestoneItem.put("type", "object");
        milestoneItem.put("additionalProperties", false);
        milestoneItem.put("required", List.of("task", "deadline"));
        milestoneItem.put("properties", Map.of(
                "task", Map.of("type", "string"),
                "deadline", Map.of("type", "string")
        ));

        milestoneSchema.put("items", milestoneItem);
        return milestoneSchema;
    }

    private Map<String, Object> buildActionItemsSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);

        Map<String, Object> roleProperties = new HashMap<>();
        List<String> roles = List.of("PM", "PD", "FE", "BE", "AI", "ALL");
        for (String role : roles) {
            roleProperties.put(role, Map.of(
                    "type", "array",
                    "items", Map.of("type", "string"),
                    "default", List.of()
            ));
        }

        schema.put("properties", roleProperties);
        schema.put("required", roles);
        return schema;
    }
}
