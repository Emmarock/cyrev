package com.cyrev.common.services;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.MailProvider;
import com.cyrev.common.dtos.Notification;
import com.cyrev.common.dtos.ProvisioningState;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {
    MailProvider getProvider();
    void sendHtmlEmail(String to, String subject, Map<String,Object> body);

    void sendTextEmail(String to, String subject, String body) throws IOException;
}
