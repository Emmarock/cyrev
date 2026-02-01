package com.cyrev.iam.adapters.bitbucket;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
public class BitbucketOAuthController {

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code) {
        return ResponseEntity.ok("Auth code received: " + code);
    }
}
