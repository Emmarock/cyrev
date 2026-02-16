package com.cyrev.common.services;

import com.cyrev.common.dtos.MailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class MicrosoftEmailNotificationService implements NotificationService {

    private final GraphMailService graphMailService;

    @Value("${mail.sender:noreply@cyrev.com}")
    private String from;

    @Override
    public void sendTextEmail(String to, String subject,String body) {
        try {
            graphMailService.sendMail(to, subject,body);
            log.info("Sent email to {}, subject {}, body {}", to, subject, body);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw e; // let Temporal retry
        }
    }

    @Override
    public MailProvider getProvider() {
        return MailProvider.MICROSOFT_GRAPH;
    }

    @Override
    public void sendHtmlEmail(String to, String subject, Map<String,Object> body) {
        graphMailService.sendMail(to, subject, body.get("html").toString());
    }
}
