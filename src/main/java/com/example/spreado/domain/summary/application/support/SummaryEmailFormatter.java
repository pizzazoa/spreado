package com.example.spreado.domain.summary.application.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public final class SummaryEmailFormatter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 역할 표시 순서 고정
    private static final List<String> ROLE_ORDER = List.of("PM", "PD", "FE", "BE", "AI", "ALL");

    private SummaryEmailFormatter() {}

    public static final class Milestone {
        public String task;
        public String deadline; // 예: "이번 주 목요일"
    }

    public static final class SummaryPayload {
        public String summary;
        public List<Milestone> milestones = List.of();
        public Map<String, List<String>> actionItemsByRole = Map.of();
    }

    public static String renderHtml(String summaryJson) {
        SummaryPayload data = parse(summaryJson);
        Map<String, List<String>> roleMap = safeRoleMap(data.actionItemsByRole);

        StringBuilder sb = new StringBuilder();
        sb.append("""
            <div style="font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;line-height:1.6;">
              <h3 style="margin:0 0 8px;">[Summary]</h3>
              <ul style="margin:0 0 16px;padding-left:22px;">
            """);
        sb.append("<li>").append(escapeHtml(nullToEmpty(data.summary))).append("</li>");
        sb.append("</ul>");

        sb.append("<h3 style=\"margin:16px 0 8px;\">[Milestones]</h3>");
        if (data.milestones == null || data.milestones.isEmpty()) {
            sb.append("<ul style=\"margin:0 0 16px;padding-left:22px;\"><li>(없음)</li></ul>");
        } else {
            sb.append("<ul style=\"margin:0 0 16px;padding-left:22px;\">");
            for (Milestone m : data.milestones) {
                String task = escapeHtml(nullToEmpty(m.task));
                String deadline = escapeHtml(nullToEmpty(m.deadline));
                if (!deadline.isBlank()) {
                    sb.append("<li>").append(task).append(" (<em>")
                            .append(deadline).append("</em>)</li>");
                } else {
                    sb.append("<li>").append(task).append("</li>");
                }
            }
            sb.append("</ul>");
        }

        sb.append("<h3 style=\"margin:16px 0 8px;\">[Action Items By Role]</h3>");
        sb.append("<div>");
        for (String role : orderedRoles(roleMap)) {
            List<String> items = roleMap.getOrDefault(role, Collections.emptyList());
            sb.append("<div style=\"margin:0 0 8px;\">");
            if (items.isEmpty()) {
                sb.append("<div>- ").append(escapeHtml(role)).append(": (없음)</div>");
            } else {
                sb.append("<div>- ").append(escapeHtml(role)).append(":</div>");
                sb.append("<ul style=\"margin:6px 0 6px 22px;padding-left:18px;\">");
                for (String it : items) {
                    sb.append("<li>").append(escapeHtml(it)).append("</li>");
                }
                sb.append("</ul>");
            }
            sb.append("</div>");
        }
        sb.append("</div>");

        sb.append("<hr style=\"margin:16px 0;border:none;border-top:1px solid #ddd;\"/>");
        sb.append("</div>");
        return sb.toString();
    }

    /* =========================
       내부 유틸
       ========================= */
    private static SummaryPayload parse(String summaryJson) {
        try {
            if (summaryJson == null) return new SummaryPayload();
            return MAPPER.readValue(summaryJson, SummaryPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid summaryJson: " + summaryJson, e);
        }
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }

    private static Map<String, List<String>> safeRoleMap(Map<String, List<String>> m) {
        if (m == null) return Collections.emptyMap();
        // 값이 null인 키가 없도록 방어
        Map<String, List<String>> out = new LinkedHashMap<>();
        m.forEach((k, v) -> out.put(k, v == null ? Collections.emptyList() : v));
        return out;
    }

    private static List<String> orderedRoles(Map<String, List<String>> roleMap) {
        // ROLE_ORDER 우선, 그 외 키는 사전순으로 뒤에
        Set<String> keys = roleMap.keySet();
        List<String> ordered = new ArrayList<>();
        for (String r : ROLE_ORDER) {
            if (keys.contains(r)) ordered.add(r);
        }
        keys.stream()
                .filter(k -> !ROLE_ORDER.contains(k))
                .sorted()
                .forEach(ordered::add);
        return ordered;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}