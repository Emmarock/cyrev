package com.cyrev.iam.service;

import com.cyrev.common.dtos.EmailEvent;
import com.cyrev.common.dtos.MailProvider;
import com.cyrev.common.services.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
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

    @EventListener
    @Async
    public void handleEmailEvent(EmailEvent event) throws IOException {
        Map<String,Object> content= event.getBody();
        if (event.isHtml()) {
            getEmailNotificationService().sendHtmlEmail(
                    event.getTo(),
                    content.get("subject").toString(),
                    content
            );
        } else {
            getEmailNotificationService().sendTextEmail(
                    event.getTo(),
                    content.get("subject").toString(),
                    content.get("body").toString()
            );
        }
    }
}
