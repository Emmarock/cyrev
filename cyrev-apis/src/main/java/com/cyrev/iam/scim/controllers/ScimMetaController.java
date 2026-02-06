package com.cyrev.iam.scim.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scim/v2")
public class ScimMetaController {

    @GetMapping("/ServiceProviderConfig")
    public Map<String, Object> spConfig() {
        return Map.of(
            "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
            "patch", Map.of("supported", true),
            "filter", Map.of("supported", true)
        );
    }

    @GetMapping("/Schemas")
    public Map<String, Object> schemas() {
        return Map.of("Resources", List.of());
    }
}
