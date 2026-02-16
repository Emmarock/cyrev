package com.cyrev.common.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class EmailEvent {

    private final String to;
    private final Map<String,Object> body;
    private final boolean html;

    public EmailEvent(String to, Map<String,Object> body, boolean html) {
        this.to = to;
        this.body = body;
        this.html = html;
    }

}
