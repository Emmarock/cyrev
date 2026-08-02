package com.cyrev.common.services;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.cyrev.common.dtos.MailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnBean(EmailClient.class)
@RequiredArgsConstructor
@Slf4j
public class AzureCommunicationEmailService implements NotificationService {

    private final EmailClient emailClient;
    private final EmailTemplateService emailTemplateService;

    @Value("${azure.communication.email.sender-address}")
    private String senderAddress;

    @Override
    public MailProvider getProvider() {
        return MailProvider.AZURE_COMMUNICATION_SERVICES;
    }

    @Override
    public void sendHtmlEmail(String to, String templatePath, Map<String, Object> body) {
        try {
            Map<String, Object> ctx = new HashMap<>(body);
            ctx.putIfAbsent("year", Year.now().getValue());
            ctx.putIfAbsent("supportEmail", "support@cyrev.com");
            ctx.putIfAbsent("email", to);

            String htmlContent = emailTemplateService.renderTemplate(templatePath, ctx);
            String subject = ctx.getOrDefault("subject", "Cyrev Notification").toString();

            send(to, subject, htmlContent, null);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email via Azure Communication Services", e);
        }
    }

    @Override
    public void sendTextEmail(String to, String subject, String body) {
        try {
            send(to, subject, "<p>" + body + "</p>", body);
        } catch (Exception e) {
            log.error("Failed to send text email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email via Azure Communication Services", e);
        }
    }

    private void send(String to, String subject, String htmlBody, String plainTextBody) {
        log.info("Sending email from {} to {} with subject {}", senderAddress, to, subject);
        EmailMessage message = new EmailMessage()
                .setSenderAddress(senderAddress)
                .setToRecipients(new EmailAddress(to))
                .setSubject(subject)
                .setBodyHtml(htmlBody);

        if (plainTextBody != null) {
            message.setBodyPlainText(plainTextBody);
        }

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> result = poller.waitForCompletion();

        EmailSendStatus status = result.getValue().getStatus();
        if (!EmailSendStatus.SUCCEEDED.equals(status)) {
            throw new RuntimeException("ACS email send did not succeed — status: " + status);
        }

        log.info("Email sent via ACS to {} (status={})", to, status);
    }
}