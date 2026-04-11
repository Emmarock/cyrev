package com.cyrev.common.services;// Example: Simple template replacement + SendGrid usage

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
@Component
public class EmailTemplateService {

    public String renderTemplate(String fileName, Map<String, Object> values) throws Exception {
        String content = loadTemplate(fileName);

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
        }

        return content;
    }


    public String loadTemplate(String name) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/" + name);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
