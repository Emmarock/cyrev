package com.cyrev.common.dtos;

public class EmailEvent {

    private final String to;
    private final String subject;
    private final String body;
    private final boolean html;

    public EmailEvent(String to, String subject, String body, boolean html) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.html = html;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isHtml() {
        return html;
    }
}
