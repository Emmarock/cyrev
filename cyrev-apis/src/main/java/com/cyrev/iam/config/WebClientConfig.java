package com.cyrev.iam.config;

import com.cyrev.iam.adapters.bitbucket.BitbucketProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Slack
    @Value("${adapters.slack.api-url}")
    private String slackApiUrl;
    @Value("${adapters.slack.token}")
    private String slackToken;

    // Jira
    @Value("${adapters.jira.api-url}")
    private String jiraApiUrl;
    @Value("${adapters.jira.username}")
    private String jiraUsername;
    @Value("${adapters.jira.api-token}")
    private String jiraToken;


    @Bean
    public WebClient slackWebClient() {
        return WebClient.builder()
                .baseUrl(slackApiUrl)
                .defaultHeader("Authorization", "Bearer " + slackToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient jiraWebClient() {
        String basicAuth = java.util.Base64.getEncoder()
                .encodeToString((jiraUsername + ":" + jiraToken).getBytes());
        return WebClient.builder()
                .baseUrl(jiraApiUrl)
                .defaultHeader("Authorization", "Basic " + basicAuth)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Primary
    public WebClient bitbucketWebClient(BitbucketProperties properties) {

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeaders(h -> h.setBasicAuth(
                        properties.getUsername(),
                        properties.getAppPassword()))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
