package com.cyrev.common.services;

import com.cyrev.common.dtos.MailProvider;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendGridEmailService implements NotificationService{

    private final SendGrid sendGrid;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Override
    public MailProvider getProvider() {
        return MailProvider.SENDGRID;
    }

    @Override
    public void sendHtmlEmail(String to, String subject, Map<String,Object> body) {

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
            String templateId,
            Map<String, Object> dynamicData
    ) throws IOException {

        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail));
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(to));

        dynamicData.forEach(personalization::addDynamicTemplateData);

        mail.addPersonalization(personalization);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("SendGrid Error: " + response.getBody());
        }
    }
}
