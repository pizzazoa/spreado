package com.example.spreado.domain.summary.core.service;

import com.example.spreado.domain.meeting.core.entity.Meeting;
import com.example.spreado.domain.meeting.core.repository.MeetingRepository;
import com.example.spreado.domain.summary.application.support.SummaryEmailFormatter;
import com.example.spreado.domain.summary.core.entity.Summary;
import com.example.spreado.domain.summary.core.repository.SummaryRepository;
import com.example.spreado.global.shared.exception.NotFoundException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final SummaryRepository summaryRepository;
    private final MeetingRepository meetingRepository;

    public void sendMail(Long summaryId) {
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new NotFoundException("해당 요약을 찾을 수 없습니다: " + summaryId));

        Meeting meeting = meetingRepository.findByNoteId(summary.getNote().getId())
                .orElseThrow(() -> new NotFoundException("해당 회의를 찾을 수 없습니다: " + summary.getNote().getId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");

        String title = "[Spreado] " + meeting.getCreatedAt().format(formatter) + " " + meeting.getTitle() + " 회의 요약본";

        List<String> recipients = meetingRepository.findParticipantEmailsByMeetingId(meeting.getId());

        String content = SummaryEmailFormatter.renderHtml(summary.getSummaryJson());

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(recipients.toArray(new String[0]));
            mimeMessageHelper.setSubject(title);
            mimeMessageHelper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
