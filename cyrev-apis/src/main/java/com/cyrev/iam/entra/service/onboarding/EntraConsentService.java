package com.cyrev.iam.entra.service.onboarding;

import com.cyrev.iam.config.EntraProperties;
import com.cyrev.iam.entra.service.utils.StatePayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EntraConsentService {

    private final EntraProperties props;
    private final ConsentStateService consentStateService;
    private final ObjectMapper objectMapper;


    public String buildUrl() {
        String state = consentStateService.generate();
        StatePayload payload = new StatePayload(
                LocalDateTime.now().plusMinutes(20),
                state
        );
        try {
            String stateJson = objectMapper.writeValueAsString(payload);
            String stateEncoded = Base64.getUrlEncoder()
                    .encodeToString(stateJson.getBytes(StandardCharsets.UTF_8));
            return props.getAuthority()
                    + "/common/adminconsent"
                    + "?client_id=" + URLEncoder.encode(props.getAppId(), StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(props.getConsentRedirectUri(), StandardCharsets.UTF_8)
                    + "&state=" + URLEncoder.encode(stateEncoded, StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}