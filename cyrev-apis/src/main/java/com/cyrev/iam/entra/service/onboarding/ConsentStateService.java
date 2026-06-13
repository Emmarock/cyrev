package com.cyrev.iam.entra.service.onboarding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ConsentStateService {

    private final Map<String, Instant> store = new ConcurrentHashMap<>();

    public String generate() {
        String state = UUID.randomUUID().toString();
        store.put(state, Instant.now());
        return state;
    }

    public void validate(String state) {
        Instant created = store.remove(state);
        if (created == null || created.plusSeconds(600).isBefore(Instant.now())) {
            log.info("Invalid or expired state {} ", state);
            throw new RuntimeException("Invalid or expired state");
        }
    }
}