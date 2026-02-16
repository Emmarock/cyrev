package com.cyrev.common.services;

import com.cyrev.common.config.GraphProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(GraphProperties.class)
public class GraphUrlBuilder {

    private final GraphProperties graphProperties;

    public String sendMailUrl(String userId) {
        return String.format(
                "%s/users/%s/sendMail",
                graphProperties.getBaseUrl(),
                userId
        );
    }
}
