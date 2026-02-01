package com.cyrev.iam.controllers;

import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.iam.domain.CyrevApiResponse;
import com.cyrev.iam.domain.UserCreationDTO;
import com.cyrev.iam.domain.UserUpdateRequestDTO;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CyrevApiResponse<List<User>>> getAllUsers() {
        var users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "User retrieved Successful",
                        users
                ));
    }

    @GetMapping()
    public ResponseEntity<CyrevApiResponse<User>> getUser(@CurrentUserId UUID id) {
        var user = userService.getUser(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "User retrieved Successful",
                        user
                ));
    }

    @PostMapping
    public ResponseEntity<CyrevApiResponse<User>> createUser(@Valid @RequestBody UserCreationDTO user) {
        var created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "User retrieved Successful",
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
