package com.example.spreado.domain.summary.application;

import com.example.spreado.global.shared.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SummaryDocumentPreprocessor {

    private final ObjectMapper objectMapper;

    public String toPlainText(JsonNode docsNode) {
        if (docsNode == null || docsNode.isNull()) {
            return "";
        }

        JsonNode root = normalizeRootNode(docsNode);
        if (root.isMissingNode() || root.isNull()) {
            throw new BadRequestException("회의록 문서가 비어 있습니다.");
        }

        if (root.isTextual()) {
            throw new BadRequestException("회의록은 tiptap JSON 형식으로 저장되어야 합니다.");
        }

        JsonNode docNode = resolveDocNode(root);
        if (docNode.isMissingNode()) {
            throw new BadRequestException("회의록 문서에서 doc.content 구조를 찾을 수 없습니다.");
        }

        StringBuilder builder = new StringBuilder();
        appendContent(docNode.path("content"), builder, 0);

        return cleanup(builder.toString());
    }

    private JsonNode normalizeRootNode(JsonNode docsNode) {
        if (!docsNode.isTextual()) {
            return docsNode;
        }

        String raw = docsNode.asText("");
        if (!StringUtils.hasText(raw)) {
            return MissingNode.getInstance();
        }

        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("노트 문서를 파싱하는 중 오류가 발생했습니다.");
        }
    }

    private JsonNode resolveDocNode(JsonNode root) {
        // 1. data.content가 문자열이면 파싱 (Liveblocks 저장 형식)
        JsonNode dataContent = root.path("data").path("content");
        if (dataContent.isTextual()) {
            try {
                JsonNode parsed = objectMapper.readTree(dataContent.asText());
                if (hasDocContent(parsed)) {
                    return parsed;
                }
            } catch (JsonProcessingException e) {
                // 파싱 실패 시 다음 경로 시도
            }
        }

        // 2. data.doc 경로
        JsonNode docNode = root.path("data").path("doc");
        if (hasDocContent(docNode)) {
            return docNode;
        }

        docNode = root.path("doc");
        if (hasDocContent(docNode)) {
            return docNode;
        }

        if (hasDocContent(root)) {
            return root;
        }

        return MissingNode.getInstance();
    }

    private boolean hasDocContent(JsonNode node) {
        return node != null
                && !node.isMissingNode()
                && !node.isNull()
                && node.path("content").isArray();
    }

    private void appendContent(JsonNode contentNode, StringBuilder builder, int indentLevel) {
        if (contentNode == null || !contentNode.isArray()) {
            return;
        }

        for (JsonNode node : contentNode) {
            String type = node.path("type").asText("");
            switch (type) {
                case "heading" -> appendHeading(node, builder);
                case "paragraph" -> appendParagraph(node, builder);
                case "bulletList" -> appendList(node, builder, indentLevel, false);
                case "orderedList" -> appendList(node, builder, indentLevel, true);
                case "blockquote" -> appendBlockquote(node, builder);
                case "taskList" -> appendTaskList(node, builder, indentLevel);
                default -> appendContent(node.path("content"), builder, indentLevel);
            }
        }
    }

    private void appendHeading(JsonNode node, StringBuilder builder) {
        int level = node.path("attrs").path("level").asInt(1);
        String headingText = collectInlineText(node.path("content"));

        if (headingText.isEmpty()) {
            return;
        }

        builder.append("#".repeat(Math.max(1, level)))
                .append(" ")
                .append(headingText.trim())
                .append("\n\n");
    }

    private void appendParagraph(JsonNode node, StringBuilder builder) {
        String paragraphText = collectInlineText(node.path("content"));
        if (paragraphText.isEmpty()) {
            return;
        }

        builder.append(paragraphText.trim())
                .append("\n\n");
    }

    private void appendBlockquote(JsonNode node, StringBuilder builder) {
        String text = collectInlineText(node.path("content"));
        if (text.isEmpty()) {
            return;
        }

        builder.append("> ")
                .append(text.trim().replaceAll("\\s*\\n\\s*", "\n> "))
                .append("\n\n");
    }

    private void appendList(JsonNode node, StringBuilder builder, int indentLevel, boolean ordered) {
        JsonNode items = node.path("content");
        if (items == null || !items.isArray()) {
            return;
        }

        int index = 1;
        for (JsonNode item : items) {
            String bullet = ordered ? index++ + ". " : "- ";
            appendListItem(item, builder, indentLevel, bullet);
        }
        builder.append("\n");
    }

    private void appendTaskList(JsonNode node, StringBuilder builder, int indentLevel) {
        JsonNode items = node.path("content");
        if (items == null || !items.isArray()) {
            return;
        }

        for (JsonNode item : items) {
            boolean checked = item.path("attrs").path("checked").asBoolean(false);
            String marker = checked ? "[x] " : "[ ] ";
            appendListItem(item, builder, indentLevel, marker);
        }
        builder.append("\n");
    }

    private void appendListItem(JsonNode item, StringBuilder builder, int indentLevel, String bullet) {
        String indent = "  ".repeat(Math.max(0, indentLevel));
        boolean hasPrimaryText = false;

        JsonNode content = item.path("content");
        if (content == null || !content.isArray()) {
            builder.append(indent)
                    .append(bullet)
                    .append("\n");
            return;
        }

        for (JsonNode child : content) {
            String type = child.path("type").asText("");
            switch (type) {
                case "paragraph" -> {
                    String text = collectInlineText(child.path("content"));
                    if (!text.isEmpty()) {
                        builder.append(indent)
                                .append(bullet)
                                .append(text.trim())
                                .append("\n");
                        hasPrimaryText = true;
                    }
                }
                case "bulletList" -> appendList(child, builder, indentLevel + 1, false);
                case "orderedList" -> appendList(child, builder, indentLevel + 1, true);
                case "taskList" -> appendTaskList(child, builder, indentLevel + 1);
                default -> appendContent(child.path("content"), builder, indentLevel + 1);
            }
        }

        if (!hasPrimaryText) {
            builder.append(indent)
                    .append(bullet.trim())
                    .append("\n");
        }
    }

    private String collectInlineText(JsonNode nodes) {
        if (nodes == null) {
            return "";
        }

        if (!nodes.isArray()) {
            return nodes.asText("");
        }

        StringBuilder text = new StringBuilder();
        for (JsonNode node : nodes) {
            String type = node.path("type").asText("");
            switch (type) {
                case "text" -> text.append(node.path("text").asText(""));
                case "hardBreak" -> text.append("\n");
                default -> text.append(collectInlineText(node.path("content")));
            }
        }

        return text.toString().replaceAll("[ \\t]{2,}", " ").trim();
    }

    private String cleanup(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }

        return text.replaceAll("(\\n){3,}", "\n\n").trim();
    }
}
