package com.cyrev.common.services;

import com.cyrev.common.dtos.MailProvider;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Year;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class SendGridEmailService implements NotificationService{

    private final SendGrid sendGrid;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Override
    public MailProvider getProvider() {
        return MailProvider.SENDGRID;
    }

    private final EmailTemplateService emailTemplateService;

    @Override
    public void sendHtmlEmail(String to, String fileName, Map<String,Object> body) {
        sendTemplateEmail(to, fileName, body);
    }

    @Override
    public void sendTextEmail(String to, String subject, String body) throws IOException {
        Email from = new Email(fromEmail);
        Email recipient = new Email(to);

        Content emailContent = new Content("text/html", body);
        Mail mail = new Mail(from, subject, recipient, emailContent);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("Failed to send email: " + response.getBody());
        }
    }

    public void sendTemplateEmail(
            String to,
            String fileName,
            Map<String, Object> dynamicData
    ) {

        try{
            dynamicData.putIfAbsent("year", Year.now().getValue());
            dynamicData.putIfAbsent("supportEmail", "support@cyrev.com");
            dynamicData.putIfAbsent("email", to);
            String content = emailTemplateService.renderTemplate(fileName, dynamicData);

            Mail mail = new Mail();
            mail.setFrom(new Email(fromEmail));
            mail.setSubject((String) dynamicData.getOrDefault("subject", "Cyrev Notification"));

            Personalization personalization = new Personalization();
            personalization.addTo(new Email(to));
            mail.addPersonalization(personalization);

            Content htmlContent = new Content("text/html", content);
            mail.addContent(htmlContent);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                log.error("SendGrid error: status={}, body={}",
                        response.getStatusCode(),
                        response.getBody());

                throw new RuntimeException("SendGrid Error: " + response.getBody());
            }
            log.info("Email sent successfully to {}", to);
        }catch(Exception e){
            log.error(e.getMessage());
        }
    }
}
