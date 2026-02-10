package com.cyrev.common.services;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.User;
import com.cyrev.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationPublisherServiceImpl implements NotificationPublisherService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

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


        EmailEvent event = new EmailEvent(manager.getEmail(),subject,body,false);
        eventPublisher.publishEvent(event);
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


        EmailEvent event = new EmailEvent(user.getEmail(),subject,body,false);
        eventPublisher.publishEvent(event);
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


        EmailEvent event = new EmailEvent(user.getEmail(),subject,body,false);
        eventPublisher.publishEvent(event);
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

        EmailEvent event = new EmailEvent(email,subject,body,false);
        eventPublisher.publishEvent(event);
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
        EmailEvent event = new EmailEvent(user.getEmail(),subject,body,false);
        eventPublisher.publishEvent(event);
    }

    @Override
    public void sendWelcomeEmail(String firstname, String email) {
        EmailEvent event = new EmailEvent(
            email, "Login Notification",
            """
            Welcome  %s 🎉,
            
            Your have successfully login on CYREV.
            
            If you did not initiate this action, please contact support immediately
            """.formatted(firstname),
            false
        );

        eventPublisher.publishEvent(event);
    }
}
