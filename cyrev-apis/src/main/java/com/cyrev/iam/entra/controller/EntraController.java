package com.cyrev.iam.entra.controller;

import com.cyrev.iam.entra.service.ApplicationService;
import com.cyrev.iam.entra.service.EntraGroupService;
import com.cyrev.iam.entra.service.EntraUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/entra")
@RequiredArgsConstructor
public class EntraController {

    private final EntraUserService entraUserService;
    private final EntraGroupService entraGroupService;
    private final ApplicationService appService;

    @PostMapping("/users")
    public Object createUser(@RequestParam String displayName,
                             @RequestParam String mailNickname,
                             @RequestParam String userPrincipalName,
                             @RequestParam String password) {
        return entraUserService.createUser(displayName, mailNickname, userPrincipalName, password);
    }

    @GetMapping("/users")
    public void listUsers() {
        entraUserService.listUsers();
    }

    @PostMapping("/groups")
    public Object createGroup(@RequestParam String displayName,
                              @RequestParam String mailNickname) {
        return entraGroupService.createGroup(displayName, mailNickname);
    }

    @PostMapping("/applications")
    public Object createApp(@RequestParam String displayName) {
        return appService.createApplication(displayName);
    }
}
