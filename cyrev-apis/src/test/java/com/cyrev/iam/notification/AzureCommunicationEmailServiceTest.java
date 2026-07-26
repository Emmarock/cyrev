package com.cyrev.iam.notification;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.cyrev.common.dtos.MailProvider;
import com.cyrev.common.services.AzureCommunicationEmailService;
import com.cyrev.common.services.EmailTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureCommunicationEmailServiceTest {

    @Mock EmailClient emailClient;
    @Mock EmailTemplateService emailTemplateService;
    @Mock SyncPoller<EmailSendResult, EmailSendResult> poller;
    @Mock PollResponse<EmailSendResult> pollResponse;
    @Mock EmailSendResult sendResult;

    AzureCommunicationEmailService service;

    @BeforeEach
    void setUp() {
        service = new AzureCommunicationEmailService(emailClient, emailTemplateService);
        ReflectionTestUtils.setField(service, "senderAddress", "DoNotReply@test.azurecomm.net");
    }

    @Test
    void getProvider_returnsAzureCommunicationServices() {
        assertThat(service.getProvider()).isEqualTo(MailProvider.AZURE_COMMUNICATION_SERVICES);
    }

    @Test
    void sendTextEmail_buildsCorrectMessageAndSends() throws Exception {
        stubSuccessfulSend();

        service.sendTextEmail("user@example.com", "Hello", "Welcome to Cyrev");

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailClient).beginSend(captor.capture());

        EmailMessage sent = captor.getValue();
        assertThat(sent.getSenderAddress()).isEqualTo("DoNotReply@test.azurecomm.net");
        assertThat(sent.getSubject()).isEqualTo("Hello");
        assertThat(sent.getBodyHtml()).contains("Welcome to Cyrev");
        assertThat(sent.getBodyPlainText()).isEqualTo("Welcome to Cyrev");
    }

    @Test
    void sendHtmlEmail_rendersTemplateAndSends() throws Exception {
        when(emailTemplateService.renderTemplate(eq("welcome.html"), any())).thenReturn("<h1>Hi Alice</h1>");
        stubSuccessfulSend();

        service.sendHtmlEmail("alice@example.com", "welcome.html",
                Map.of("firstname", "Alice", "subject", "Welcome!"));

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailClient).beginSend(captor.capture());

        EmailMessage sent = captor.getValue();
        assertThat(sent.getSenderAddress()).isEqualTo("DoNotReply@test.azurecomm.net");
        assertThat(sent.getSubject()).isEqualTo("Welcome!");
        assertThat(sent.getBodyHtml()).isEqualTo("<h1>Hi Alice</h1>");
    }

    @Test
    void sendHtmlEmail_defaultsSubjectWhenNotProvided() throws Exception {
        when(emailTemplateService.renderTemplate(any(), any())).thenReturn("<p>body</p>");
        stubSuccessfulSend();

        service.sendHtmlEmail("user@example.com", "notify.html", Map.of());

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailClient).beginSend(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Cyrev Notification");
    }

    @Test
    void sendTextEmail_throwsWhenAcsReturnsNonSucceededStatus() {
        when(emailClient.beginSend(any())).thenReturn(poller);
        when(poller.waitForCompletion()).thenReturn(pollResponse);
        when(pollResponse.getValue()).thenReturn(sendResult);
        when(sendResult.getStatus()).thenReturn(EmailSendStatus.FAILED);

        assertThatThrownBy(() -> service.sendTextEmail("user@example.com", "Subject", "Body"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Azure Communication Services");
    }

    @Test
    void sendTextEmail_throwsWhenEmailClientThrows() {
        when(emailClient.beginSend(any())).thenThrow(new RuntimeException("ACS unavailable"));

        assertThatThrownBy(() -> service.sendTextEmail("user@example.com", "Subject", "Body"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Azure Communication Services");
    }

    @Test
    void sendHtmlEmail_throwsWhenTemplateRenderingFails() throws Exception {
        when(emailTemplateService.renderTemplate(any(), any())).thenThrow(new Exception("Template not found"));

        assertThatThrownBy(() -> service.sendHtmlEmail("user@example.com", "missing.html", Map.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Azure Communication Services");

        verifyNoInteractions(emailClient);
    }

    // --- helpers ---

    private void stubSuccessfulSend() {
        when(emailClient.beginSend(any())).thenReturn(poller);
        when(poller.waitForCompletion()).thenReturn(pollResponse);
        when(pollResponse.getValue()).thenReturn(sendResult);
        when(sendResult.getStatus()).thenReturn(EmailSendStatus.SUCCEEDED);
    }
}