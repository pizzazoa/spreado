package com.example.spreado.domain.group.core.service;

import com.example.spreado.domain.group.core.entity.Group;
import com.example.spreado.domain.user.core.entity.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupEmailInviteService {

    private final JavaMailSender mailSender;

    public void sendInviteEmails(Group group, User inviter, List<String> emails, String customMessage) {
        String subject = String.format("[Spreado] %s님이 '%s' 그룹에 초대했습니다", inviter.getName(), group.getName());
        String htmlContent = buildInviteEmailHtml(group, inviter, customMessage);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(emails.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private String buildInviteEmailHtml(Group group, User inviter, String customMessage) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='ko'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }");
        html.append(".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; }");
        html.append(".content { padding: 30px; }");
        html.append(".invite-box { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; border-radius: 4px; }");
        html.append(".group-name { font-size: 24px; font-weight: 600; color: #667eea; margin: 0 0 10px 0; }");
        html.append(".inviter-name { font-size: 16px; color: #666; margin-bottom: 20px; }");
        html.append(".message-box { background-color: #fff; border: 1px solid #e0e0e0; padding: 15px; margin: 20px 0; border-radius: 4px; font-style: italic; color: #555; }");
        html.append(".invite-link { background-color: #667eea; color: #ffffff; text-decoration: none; padding: 12px 30px; border-radius: 6px; display: inline-block; margin: 20px 0; font-weight: 600; transition: background-color 0.3s; }");
        html.append(".invite-link:hover { background-color: #5568d3; }");
        html.append(".footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 14px; color: #666; }");
        html.append(".footer a { color: #667eea; text-decoration: none; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>🎉 그룹 초대장</h1>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<div class='invite-box'>");
        html.append("<p class='group-name'>").append(escapeHtml(group.getName())).append("</p>");
        html.append("<p class='inviter-name'>").append(escapeHtml(inviter.getName())).append("님이 초대했습니다</p>");
        html.append("</div>");

        // Custom Message
        if (customMessage != null && !customMessage.isBlank()) {
            html.append("<div class='message-box'>");
            html.append("<p style='margin: 0;'><strong>초대 메시지:</strong></p>");
            html.append("<p style='margin: 10px 0 0 0;'>").append(escapeHtml(customMessage)).append("</p>");
            html.append("</div>");
        }

        html.append("<p>Spreado를 통해 팀원들과 함께 효율적으로 회의를 관리하고 협업하세요.</p>");
        html.append("<p>아래 버튼을 클릭하여 그룹에 참여할 수 있습니다:</p>");

        // Invite Link Button
        html.append("<div style='text-align: center;'>");
        html.append("<a href='").append(escapeHtml(group.getInviteLink())).append("' class='invite-link'>");
        html.append("그룹 참여하기");
        html.append("</a>");
        html.append("</div>");

        html.append("<p style='margin-top: 30px; font-size: 14px; color: #666;'>");
        html.append("또는 아래 링크를 복사하여 브라우저에 붙여넣으세요:<br>");
        html.append("<code style='background-color: #f4f4f4; padding: 5px 10px; border-radius: 3px; display: inline-block; margin-top: 10px;'>");
        html.append(escapeHtml(group.getInviteLink()));
        html.append("</code>");
        html.append("</p>");

        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>이 이메일은 Spreado 그룹 초대를 위해 발송되었습니다.</p>");
        html.append("<p>&copy; 2025 Spreado. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
