package com.cyrev.iam.adapters.bitbucket;

import com.cyrev.common.services.AppProvisioningAdapter;
import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Role;
import com.cyrev.common.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class BitbucketAdapter implements AppProvisioningAdapter {

 private final WebClient bitbucketWebClient;
 private final BitbucketProperties bitbucketProperties;

 public BitbucketAdapter(@Qualifier("bitbucketWebClient") WebClient bitbucketWebClient, BitbucketProperties bitbucketProperties) {
  this.bitbucketWebClient = bitbucketWebClient;
  this.bitbucketProperties = bitbucketProperties;
 }

 @Override
 public App app() {
  return App.BITBUCKET;
 }

 @Override
 public void createUser(User user, Role role) {
   createUserNonBlocking(user).block();
 }

 public Mono<Void> createUserNonBlocking(User user) {
  return bitbucketWebClient
          .put()
          .uri("/workspaces/{workspace}/members/{email}",
                  bitbucketProperties.getWorkspace(),
                  user.getEmail())
          .retrieve()
          .onStatus(
                  status -> status.value() == 403,
                  response -> Mono.error(
                          new IllegalStateException("Bitbucket permission denied"))
          )
          .onStatus(
                  status -> status.value() == 429,
                  response -> Mono.error(
                          new IllegalStateException("Bitbucket rate limited"))
          )
          .bodyToMono(Void.class)
          .doOnSuccess(v ->
                  log.info("Bitbucket user invite sent for {}", user.getEmail()))
          .doOnError(e ->
                  log.error("Failed to invite Bitbucket user {}", user.getEmail(), e));
 }

 @Override
 public void assignUser(User user, Role role) {
  String accountId = fetchExternalUserIdByEmail(user.getEmail())
          .orElseThrow();

  bitbucketWebClient.post()
          .uri("https://api.bitbucket.org/2.0/teams/yourteam/members")
          .bodyValue(Map.of("email", user.getEmail()))
          .retrieve()
          .bodyToMono(Void.class)
          .block();
 }

 @Override
 public void revokeUser(User user) {
  fetchExternalUserIdByEmail(user.getEmail())
          .ifPresent(accountId ->
                  bitbucketWebClient.post()
                          .uri("/2.0/workspaces/my-ws/members/" + accountId)
                          .retrieve()
                          .bodyToMono(Void.class)
                          .block()
          );
 }

 @Override
 public Optional<String> fetchExternalUserIdByEmail(String email) {
  return Optional.empty();
 }
}

