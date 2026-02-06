package com.cyrev.iam.scim.security;

import java.util.Optional;

public class ScimFilter {

    public static Optional<String> extractEquals(String filter) {
        if (filter == null) return Optional.empty();

        // externalId eq "xxx"
        String[] parts = filter.split(" eq ");
        if (parts.length != 2) return Optional.empty();

        return Optional.of(parts[1].replace("\"", "").trim());
    }
}
