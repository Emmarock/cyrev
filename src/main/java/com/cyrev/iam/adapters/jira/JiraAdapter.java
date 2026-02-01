package com.cyrev.iam.adapters.jira;

import com.cyrev.iam.adapters.AppProvisioningAdapter;
import com.cyrev.iam.domain.App;
import com.cyrev.iam.domain.Role;
import com.cyrev.iam.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JiraAdapter implements AppProvisioningAdapter {

 private final WebClient jiraWebClient;


 @Override
 public App app() {
  return App.JIRA;
 }

 @Override
 public void createUser(User user, Role role) {
  if (fetchExternalUserIdByEmail(user.getEmail()).isPresent()) {
   return;
  }

  jiraWebClient.post()
          .uri("/rest/api/3/user")
          .bodyValue(Map.of(
                  "emailAddress", user.getEmail(),
                  "displayName", user.getFirstName() + " " + user.getLastName()
          ))
          .retrieve()
          .bodyToMono(Void.class).block();
 }

 @Override
 public void assignUser(User user, Role role) {
  createUser(user, role);

  jiraWebClient.post()
          .uri("/rest/api/3/group/user?groupname=jira-software-users")
          .bodyValue(Map.of("accountId",
                  fetchExternalUserIdByEmail(user.getEmail()).orElseThrow()
          ))
          .retrieve()
          .bodyToMono(Void.class)
          .block();
 }

 @Override
 public void revokeUser(User user) {
  fetchExternalUserIdByEmail(user.getEmail())
          .ifPresent(accountId ->
                  jiraWebClient.delete()
                          .uri("/rest/api/3/group/user?groupname=jira-software-users&accountId=" + accountId)
                          .retrieve()
                          .bodyToMono(Void.class)
                  .block()
          );
 }

 @Override
 public Optional<String> fetchExternalUserIdByEmail(String email) {
  // Atlassian search omitted for brevity
  return Optional.empty();
 }
}

