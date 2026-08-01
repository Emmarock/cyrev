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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        ReflectionTestUtils.setField(listener, "activeProvider", "AZURE_COMMUNICATION_SERVICES");
        listener.init();
        clearInvocations(acsService, sendGridService);
    }

    @Test
    void routesToAcsForTextEmail() throws Exception {
        listener.handleEmailEvent(new EmailEvent("user@example.com",
                Map.of("subject", "Test Subject", "body", "Test body")));

        verify(acsService).sendTextEmail("user@example.com", "Test Subject", "Test body");
        verifyNoInteractions(sendGridService);
    }

    @Test
    void routesToAcsForHtmlEmail() throws Exception {
        listener.handleEmailEvent(new EmailEvent("user@example.com", "welcome.html",
                Map.of("firstname", "Alice", "subject", "Welcome")));

        verify(acsService).sendHtmlEmail("user@example.com", "welcome.html",
                Map.of("firstname", "Alice", "subject", "Welcome"));
        verifyNoInteractions(sendGridService);
    }

    @Test
    void routesToSendGridWhenProviderSwitched() throws Exception {
        ReflectionTestUtils.setField(listener, "activeProvider", "SENDGRID");

        listener.handleEmailEvent(new EmailEvent("user@example.com",
                Map.of("subject", "Hello", "body", "World")));

        verify(sendGridService).sendTextEmail("user@example.com", "Hello", "World");
        verifyNoInteractions(acsService);
    }

    @Test
    void throwsForUnknownProvider() {
        ReflectionTestUtils.setField(listener, "activeProvider", "CARRIER_PIGEON");

        assertThatThrownBy(() -> listener.handleEmailEvent(
                new EmailEvent("user@example.com", Map.of("subject", "s", "body", "b"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unknown mail provider");
    }

    @Test
    void throwsWhenProviderNotRegistered() {
        ReflectionTestUtils.setField(listener, "activeProvider", "MICROSOFT_GRAPH");

        assertThatThrownBy(() -> listener.handleEmailEvent(
                new EmailEvent("user@example.com", Map.of("subject", "s", "body", "b"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No NotificationService bean is registered");
    }
}