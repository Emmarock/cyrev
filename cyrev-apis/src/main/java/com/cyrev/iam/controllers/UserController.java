package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.*;
import com.cyrev.common.entities.TenantContext;
import com.cyrev.common.entities.TenantContextHolder;
import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.common.entities.User;
import com.cyrev.iam.annotations.TenantAdmin;
import com.cyrev.iam.service.InviteService;
import com.cyrev.iam.service.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final InviteService inviteService;
    public UserController(UserService userService, InviteService inviteService) {
        this.userService = userService;
        this.inviteService = inviteService;
    }

    @PostMapping("/invites")
    public ResponseEntity<UserInviteDTO> inviteUser(@CurrentUserId UUID inviter, @RequestBody InviteUserRequest request) {
        return ResponseEntity.ok(inviteService.sendInvite(inviter,request));
    }

    @PostMapping("/invites/accept")
    public ResponseEntity<AcceptInviteDTO> acceptInvite(@RequestBody AcceptInviteRequest request) {
        AcceptInviteDTO acceptInviteDTO = inviteService.acceptInvite(request);
        return ResponseEntity.ok(acceptInviteDTO);
    }
    @GetMapping("/all")
    @TenantAdmin
    public ResponseEntity<CyrevApiResponse<List<User>>> getAllUsers() {
        TenantContext tenant = TenantContextHolder.get();
        var users = userService.getTenantAllUsers(tenant.getInternalTenantId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "User retrieved Successful",
                        users
                ));
    }

    @GetMapping()
    public ResponseEntity<CyrevApiResponse<User>> getUser(@CurrentUserId UUID id) {
        TenantContext tenant = TenantContextHolder.get();
        var user = userService.findTenantUser(id,tenant.getInternalTenantId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "User retrieved Successful",
                        user
                ));
    }


    @PostMapping
    public ResponseEntity<CyrevApiResponse<User>> createUser(@Valid @RequestBody UserCreationDTO user) throws BadRequestException {
        TenantContext tenant = TenantContextHolder.get();
        var created = userService.createUser(tenant!=null?tenant.getEntraTenantId():null, user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "User created Successful",
                        created
                ));
    }

    @PutMapping()
    public ResponseEntity<CyrevApiResponse<User>> updateUser(@CurrentUserId UUID id,
                                                             @RequestBody UserUpdateRequestDTO updated) {
        var response =  userService.updateUser(id, updated);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "User updated Successful",
                        response
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CyrevApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
