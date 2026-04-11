package com.cyrev.common.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class EmailEvent {

    private final String to;
    private String fileName;
    private final Map<String,Object> body;

    public EmailEvent(String to, String fileName, Map<String,Object> body) {
        this.to = to;
        this.fileName = fileName;
        this.body = body;
    }

    public EmailEvent(String to, Map<String, Object> body) {
        this.body = body;
        this.to = to;
    }
}
