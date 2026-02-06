package com.cyrev.iam.exceptions;

public class ScimNotFoundException extends Exception {
    public ScimNotFoundException() {
    }

    public ScimNotFoundException(String message) {
        super(message);
    }
}
