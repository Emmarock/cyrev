package com.cyrev.iam.scim.controllers;

import com.cyrev.iam.exceptions.ScimNotFoundException;
import com.cyrev.iam.scim.dto.ScimListResponse;
import com.cyrev.iam.scim.dto.ScimPatchRequest;
import com.cyrev.iam.scim.dto.ScimUserRequest;
import com.cyrev.iam.scim.dto.ScimUserResponse;
import com.cyrev.iam.scim.service.ScimUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scim/v2/Users")
@RequiredArgsConstructor
public class ScimUserController {

    private final ScimUserService userService;

    @PostMapping
    public ResponseEntity<ScimUserResponse> create(@RequestBody ScimUserRequest req) {
        return ResponseEntity.ok(userService.create(req));
    }

    @GetMapping
    public ScimListResponse<ScimUserResponse> filter(@RequestParam String filter) {
        return userService.filter(filter);
    }

    @GetMapping("/{id}")
    public ScimListResponse<ScimUserResponse> getUserById(@PathVariable("id") String id) {
        return userService.filter(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScimUserResponse> patch(
            @PathVariable String id,
            @RequestBody ScimPatchRequest req) throws ScimNotFoundException {
        return ResponseEntity.ok(userService.patchUser(id, req));
    }


}
