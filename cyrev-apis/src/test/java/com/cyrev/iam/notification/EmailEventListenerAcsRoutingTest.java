package com.cyrev.iam.notification;

import com.cyrev.common.dtos.EmailEvent;
import com.cyrev.common.dtos.MailProvider;
import com.cyrev.common.services.NotificationService;
import com.cyrev.iam.service.EmailEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailEventListenerAcsRoutingTest {

    @Mock NotificationService acsService;
    @Mock NotificationService sendGridService;

    EmailEventListener listener;

    @BeforeEach
    void setUp() {
        when(acsService.getProvider()).thenReturn(MailProvider.AZURE_COMMUNICATION_SERVICES);
        when(sendGridService.getProvider()).thenReturn(MailProvider.SENDGRID);
        listener = new EmailEventListener(List.of(acsService, sendGridService));
        listener.init();
        // clear the getProvider() calls made during init so verifyNoInteractions is clean
        clearInvocations(acsService, sendGridService);
    }

    @Test
    void handleEmailEvent_routesToAcsForTextEmail() throws Exception {
        EmailEvent event = new EmailEvent("user@example.com",
                Map.of("subject", "Test Subject", "body", "Test body"));

        listener.handleEmailEvent(event);

        verify(acsService).sendTextEmail("user@example.com", "Test Subject", "Test body");
        verifyNoInteractions(sendGridService);
    }

    @Test
    void handleEmailEvent_routesToAcsForHtmlEmail() throws Exception {
        EmailEvent event = new EmailEvent("user@example.com", "welcome.html",
                Map.of("firstname", "Alice", "subject", "Welcome"));

        listener.handleEmailEvent(event);

        verify(acsService).sendHtmlEmail("user@example.com", "welcome.html",
                Map.of("firstname", "Alice", "subject", "Welcome"));
        verifyNoInteractions(sendGridService);
    }

    @Test
    void handleEmailEvent_doesNotRouteToSendGrid() throws Exception {
        EmailEvent event = new EmailEvent("user@example.com",
                Map.of("subject", "Hello", "body", "World"));

        listener.handleEmailEvent(event);

        verifyNoInteractions(sendGridService);
    }
}