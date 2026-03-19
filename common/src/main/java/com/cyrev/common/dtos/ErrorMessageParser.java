package com.cyrev.common.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ErrorMessageParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ParsedError parse(String message) {
        if (message == null) {
            return new ParsedError("Unknown error", null);
        }

        int jsonStart = message.indexOf("{");

        if (jsonStart == -1) {
            return new ParsedError(message, null);
        }

        String cleanMessage = message.substring(0, jsonStart).trim();
        String possibleJson = message.substring(jsonStart);

        Object details = tryParseJson(possibleJson);

        // If parsing failed, treat entire message as plain text
        if (details == null) {
            return new ParsedError(message, null);
        }

        return new ParsedError(cleanMessage, details);
    }

    private static Object tryParseJson(String str) {
        try {
            return mapper.readTree(str); // returns JsonNode
        } catch (Exception e) {
            return str; // fallback to raw string
        }
    }

    public static class ParsedError {
        private final String message;
        private final Object details;

        public ParsedError(String message, Object details) {
            this.message = message;
            this.details = details;
        }

        public String getMessage() { return message; }
        public Object getDetails() { return details; }
    }
}