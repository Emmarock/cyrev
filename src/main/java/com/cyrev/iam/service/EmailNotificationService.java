package com.cyrev.iam.service;

import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Notification;
import com.cyrev.iam.domain.NotificationType;
import com.cyrev.iam.domain.ProvisioningState;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${cyrev.mail.from:noreply@cyrev.com}")
    private String from;

    @Override
    public void sendApprovalRequest(UUID userId, Set<App> apps) {
        User user = userRepository.findById(userId).orElseThrow();
        User manager = user.getManager();

        String subject = "Approval Required: App Access Request";
        String body = """
                User %s (%s) has requested access to the following apps:

                %s

                Please review and approve in Cyrev IAM.
                """
                .formatted(
                        user.getFirstName(),
                        user.getEmail(),
                        apps.stream().map(Enum::name).collect(Collectors.joining(", "))
                );

        send(manager.getEmail(), subject, body);
    }

    @Override
    public void sendApprovalDecision(Notification notification) {
        User user = userRepository.findById(notification.getUserId()).orElseThrow();

        String subject = NotificationType.PROVISIONING_COMPLETED.equals(notification.getType())
                ? "Your access request was approved"
                : "Your access request was rejected";

        String body =  NotificationType.PROVISIONING_COMPLETED.equals(notification.getType())
                ? "Your app access request has been approved."
                : "Your app access request was rejected.\nReason: " + notification.getMessage();

        send(user.getEmail(), subject, body);
    }

    @Override
    public void sendProvisioningComplete(
            UUID userId,
            ProvisioningState state
    ) {
        User user = userRepository.findById(userId).orElseThrow();

        String subject = "App Provisioning Status: " + state.name();

        String body = """
                Hello %s,

                Your app provisioning process is complete.

                Status: %s

                If you experience any issues, contact support.
                """
                .formatted(user.getFirstName(), state.name());

        send(user.getEmail(), subject, body);
    }

    @Override
    public void sendUserActivated(String email, String firstName, String password) {
        String subject = "User Activation";

        String body = """
                Hello %s,

                Your CYREV user portal invitation .

                Password: %s
                
                Please change this password at your first sign in

                If you experience any issues, contact support.
                """
                .formatted(firstName, password);
        send(email, subject, body);
    }

    @Override
    public void sendWelcomeNotification(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();

        String subject = "Welcome Notification";

        String body = """
                Hello %s,

                Your have been successfully provisioned on CYREV.

                If you experience any issues, contact support.
                """
                .formatted(user.getFirstName());

        send(user.getEmail(), subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Sent email to {}, subject {}, body {}", to, subject, body);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw e; // let Temporal retry
        }
    }
}
