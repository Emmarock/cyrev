package com.cyrev.common.services;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Notification;
import com.cyrev.common.dtos.ProvisioningState;

import java.util.Set;
import java.util.UUID;

public interface NotificationService {

    void sendHtmlEmail(String to, String subject, String body);

    void sendTextEmail(String to, String subject, String body);
}
