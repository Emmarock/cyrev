package com.cyrev.iam.service;

import com.cyrev.common.dtos.EmailEvent;
import com.cyrev.common.dtos.MailProvider;
import com.cyrev.common.services.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListener {

    private final List<NotificationService> emailNotificationServices;
    private final Map<MailProvider, NotificationService> emailNotificationServiceMap = new HashMap<>();

    @Value("${mail.provider:AZURE_COMMUNICATION_SERVICES}")
    private String activeProvider;

    @PostConstruct
    public void init(){
        emailNotificationServices.forEach(notificationService -> emailNotificationServiceMap.put(notificationService.getProvider(), notificationService));
        log.info("Active email provider: {}", activeProvider);
    }

    NotificationService getEmailNotificationService(){
        MailProvider provider;
        try {
            provider = MailProvider.valueOf(activeProvider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown mail provider configured: '" + activeProvider +
                "'. Valid values: AZURE_COMMUNICATION_SERVICES, SENDGRID, MICROSOFT_GRAPH");
        }
        NotificationService svc = emailNotificationServiceMap.get(provider);
        if (svc == null) {
            throw new IllegalStateException("No NotificationService bean is registered for provider '" + provider +
                "'. Check that the corresponding service is enabled and its required env vars are set.");
        }
        return svc;
    }

    @Async
    @EventListener
    public void handleEmailEvent(EmailEvent event) throws IOException {
        Map<String,Object> content= event.getBody();
        log.info("Received email event for user {}",event.getTo());
        if (event.getFileName() != null) {
            getEmailNotificationService().sendHtmlEmail(
                    event.getTo(),
                    event.getFileName(),
                    content
            );
        } else {
            getEmailNotificationService().sendTextEmail(
                    event.getTo(),
                    content.get("subject").toString(),
                    content.get("body").toString()
            );
        }
        log.info("Email has been sent to user {}",event.getTo());
    }
}
