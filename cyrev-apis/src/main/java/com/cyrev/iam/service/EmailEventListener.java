package com.cyrev.iam.service;

import com.cyrev.common.dtos.EmailEvent;
import com.cyrev.common.dtos.MailProvider;
import com.cyrev.common.services.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostConstruct
    public void init(){
        emailNotificationServices.forEach(notificationService -> emailNotificationServiceMap.put(notificationService.getProvider(), notificationService));
    }

    NotificationService getEmailNotificationService(){
        return emailNotificationServiceMap.get(MailProvider.SENDGRID);
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
