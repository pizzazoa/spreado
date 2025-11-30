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
            <div style="max-width:680px;margin:0 auto;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;color:#1a1a1a;background:#ffffff;">
              
              <!-- Summary Section -->
              <div style="margin-bottom:32px;">
                <h2 style="margin:0 0 16px;font-size:20px;font-weight:600;color:#0066cc;border-bottom:3px solid #0066cc;padding-bottom:8px;display:inline-block;">
                  📋 Summary
                </h2>
                <div style="background:#f8f9fa;border-left:4px solid #0066cc;padding:16px 20px;border-radius:4px;margin-top:12px;">
                  <p style="margin:0;font-size:15px;line-height:1.7;color:#2c3e50;">
            """);
        sb.append(escapeHtml(nullToEmpty(data.summary)));
        sb.append("""
                  </p>
                </div>
              </div>
            """);

        // Milestones Section
        sb.append("""
              <!-- Milestones Section -->
              <div style="margin-bottom:32px;">
                <h2 style="margin:0 0 16px;font-size:20px;font-weight:600;color:#28a745;border-bottom:3px solid #28a745;padding-bottom:8px;display:inline-block;">
                  🎯 Milestones
                </h2>
            """);

        if (data.milestones == null || data.milestones.isEmpty()) {
            sb.append("""
                <div style="background:#f8f9fa;padding:16px 20px;border-radius:4px;border-left:4px solid #dee2e6;margin-top:12px;">
                  <p style="margin:0;color:#6c757d;font-style:italic;">등록된 마일스톤이 없습니다.</p>
                </div>
            """);
        } else {
            sb.append("<div style=\"margin-top:12px;\">");
            for (int i = 0; i < data.milestones.size(); i++) {
                Milestone m = data.milestones.get(i);
                String task = escapeHtml(nullToEmpty(m.task));
                String deadline = escapeHtml(nullToEmpty(m.deadline));

                sb.append("<div style=\"background:#ffffff;border:1px solid #e0e0e0;border-radius:6px;padding:14px 18px;margin-bottom:10px;box-shadow:0 1px 3px rgba(0,0,0,0.05);\">");
                sb.append("<div style=\"display:flex;align-items:start;\">");
                sb.append("<span style=\"color:#28a745;font-weight:600;margin-right:10px;font-size:15px;\">▸</span>");
                sb.append("<div style=\"flex:1;\">");
                sb.append("<span style=\"font-size:15px;color:#2c3e50;\">").append(task).append("</span>");
                if (!deadline.isBlank()) {
                    sb.append("<div style=\"margin-top:6px;\">");
                    sb.append("<span style=\"display:inline-block;background:#fff3cd;color:#856404;padding:3px 10px;border-radius:12px;font-size:13px;font-weight:500;\">");
                    sb.append("⏱ ").append(deadline);
                    sb.append("</span></div>");
                }
                sb.append("</div></div></div>");
            }
            sb.append("</div>");
        }
        sb.append("</div>");

        // Action Items Section
        sb.append("""
              <!-- Action Items Section -->
              <div style="margin-bottom:32px;">
                <h2 style="margin:0 0 16px;font-size:20px;font-weight:600;color:#dc3545;border-bottom:3px solid #dc3545;padding-bottom:8px;display:inline-block;">
                  ✅ Action Items By Role
                </h2>
                <div style="margin-top:12px;">
            """);

        for (String role : orderedRoles(roleMap)) {
            List<String> items = roleMap.getOrDefault(role, Collections.emptyList());
            String roleColor = getRoleColor(role);

            sb.append("<div style=\"background:#ffffff;border:1px solid #e0e0e0;border-radius:6px;padding:16px 20px;margin-bottom:12px;box-shadow:0 1px 3px rgba(0,0,0,0.05);\">");
            sb.append("<div style=\"display:flex;align-items:center;margin-bottom:").append(items.isEmpty() ? "0" : "12px").append(";\">");
            sb.append("<span style=\"display:inline-block;background:").append(roleColor).append(";color:#ffffff;padding:4px 12px;border-radius:4px;font-weight:600;font-size:13px;margin-right:10px;\">");
            sb.append(escapeHtml(role));
            sb.append("</span>");

            if (items.isEmpty()) {
                sb.append("<span style=\"color:#6c757d;font-style:italic;font-size:14px;\">할당된 액션 아이템이 없습니다.</span>");
            }
            sb.append("</div>");

            if (!items.isEmpty()) {
                sb.append("<ul style=\"margin:0;padding:0;list-style:none;\">");
                for (String it : items) {
                    sb.append("<li style=\"padding:8px 0;border-bottom:1px solid #f0f0f0;font-size:14px;color:#2c3e50;line-height:1.6;\">");
                    sb.append("<span style=\"color:").append(roleColor).append(";margin-right:8px;font-weight:600;\">•</span>");
                    sb.append(escapeHtml(it));
                    sb.append("</li>");
                }
                sb.append("</ul>");
            }
            sb.append("</div>");
        }

        sb.append("""
                </div>
              </div>
              
              <!-- Footer -->
              <div style="margin-top:40px;padding-top:20px;border-top:1px solid #e0e0e0;text-align:center;">
                <p style="margin:0;font-size:13px;color:#6c757d;">
                  이 메일은 회의 요약 시스템에서 자동 생성되었습니다.
                </p>
              </div>
              
            </div>
            """);

        return sb.toString();
    }

    /* =========================
       내부 유틸
       ========================= */
    private static String getRoleColor(String role) {
        return switch (role) {
            case "PM" -> "#0066cc";
            case "PD" -> "#6f42c1";
            case "FE" -> "#fd7e14";
            case "BE" -> "#28a745";
            case "AI" -> "#dc3545";
            case "ALL" -> "#6c757d";
            default -> "#17a2b8";
        };
    }

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