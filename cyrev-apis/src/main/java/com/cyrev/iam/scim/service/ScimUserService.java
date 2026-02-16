package com.cyrev.iam.scim.service;

import com.cyrev.common.dtos.UserStatus;
import com.cyrev.common.entities.User;
import com.cyrev.iam.exceptions.ScimNotFoundException;
import com.cyrev.common.repository.UserRepository;
import com.cyrev.iam.scim.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScimUserService {
    private final UserRepository userRepository;
    private final ScimUserFilterParser parser;

    @Transactional
    public ScimUserResponse create(ScimUserRequest request) {

        // 1️⃣ Try externalId first (authoritative)
        Optional<User> existingByExternalId =
                Optional.ofNullable(request.getExternalId())
                        .flatMap(userRepository::findByUsername);

        if (existingByExternalId.isPresent()) {
            return toScim(existingByExternalId.get());
        }

        // 2️⃣ Fallback to username/email
        String email = request.getUserName().toLowerCase();

        Optional<User> existingByEmail =
                userRepository.findByEmail(email);

        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            userRepository.save(user);
            return toScim(user);
        }

        // 3️⃣ Create new user
        User user = new User();
        user.setEmail(email);
        user.setStatus(Boolean.TRUE.equals(request.getActive())?UserStatus.ACTIVE:UserStatus.INACTIVE);
        user.setUsername(request.getUserName());
        user = userRepository.save(user);

        return toScim(user);
    }

    public ScimListResponse<ScimUserResponse> filter(String filter) {

        List<User> users = parser.parse(filter)
                .flatMap(f -> switch (f.getAttribute()) {
                    case "userName" -> userRepository.findByEmail(f.getValue()).map(List::of);
                    //case "externalId" -> userRepository.findByExternalId(f.getValue()).map(List::of);
                    case "id" -> userRepository.findById(UUID.fromString(f.getValue())).map(List::of);
                    default -> Optional.empty();
                })
                .orElse(List.of());

        return toListResponse(users);
    }

    private ScimListResponse<ScimUserResponse> toListResponse(List<User> users) {
        ScimListResponse<ScimUserResponse> res = new ScimListResponse<>();
        res.setTotalResults(users.size());
        res.setResources(
                users.stream()
                        .map(this::toScim)
                        .toList()
        );
        return res;
    }

    private ScimUserResponse toScim(User u) {
        return new ScimUserResponse(
                u.getId().toString(),
                u.getEmail(),
                u.getStatus()== UserStatus.ACTIVE,
                new Meta("User")
        );
    }

    @Transactional
    public ScimUserResponse patchUser(String scimId, ScimPatchRequest request) throws ScimNotFoundException {

        User user = userRepository.findByUsername(scimId) // update later
                .orElseThrow(() -> new ScimNotFoundException("User not found"));

        for (ScimPatchRequest.PatchOperation op : request.getOperations()) {

            if (!"replace".equalsIgnoreCase(op.getOp())) {
                continue; // Entra only uses replace
            }

            applyReplace(user, op);
        }

        return toScim(userRepository.save(user));
    }

    private void applyReplace(User user, ScimPatchRequest.PatchOperation op) {

        if (op.getPath() == null) return;

        switch (op.getPath()) {

            case "active" -> {
                user.setStatus(Boolean.parseBoolean(op.getValue().toString())?UserStatus.ACTIVE:UserStatus.INACTIVE);
            }

            case "userName" -> {
                String newUsername = op.getValue().toString();
                user.setUsername(newUsername);
                user.setEmail(newUsername); // if you mirror
            }

            default -> {
                // Ignore unknown fields (SCIM spec compliant)
            }
        }
    }


}
