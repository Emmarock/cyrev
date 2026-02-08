package com.cyrev.iam.service;

import com.cyrev.common.dtos.EmailEvent;
import com.cyrev.common.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final NotificationService emailService;

    @EventListener
    @Async
    public void handleEmailEvent(EmailEvent event) {
        if (event.isHtml()) {
            emailService.sendHtmlEmail(
                    event.getTo(),
                    event.getSubject(),
                    event.getBody()
            );
        } else {
            emailService.sendTextEmail(
                    event.getTo(),
                    event.getSubject(),
                    event.getBody()
            );
        }
    }
}
