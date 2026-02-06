package com.cyrev.iam.scim.service;

import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ScimUserFilterParser {

    Pattern SCIM_SINGLE_FILTER = Pattern.compile(
            "^([a-zA-Z0-9_.-]+)\\s+(eq|ne|co|sw|ew|pr)\\s*(?:\"([^\"]*)\")?$",
            Pattern.CASE_INSENSITIVE
    );
    public Optional<ScimFilter> parse(String filter) {

        Matcher matcher = SCIM_SINGLE_FILTER.matcher(filter);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        return Optional.of(
            new ScimFilter(matcher.group(1), matcher.group(3))
        );
    }

    @Value
    public static class ScimFilter {
        String attribute;
        String value;
    }
}
