package com.cyrev.iam.scim.service;

import com.cyrev.iam.entities.Group;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.exceptions.ScimNotFoundException;
import com.cyrev.iam.repository.GroupRepository;
import com.cyrev.iam.repository.UserRepository;
import com.cyrev.iam.scim.dto.ScimGroupRequest;
import com.cyrev.iam.scim.dto.ScimPatchRequest;
import com.cyrev.iam.scim.security.ScimFilter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScimGroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public Group createGroup(ScimGroupRequest request) {

        // Check if group already exists (idempotent)
        Group group = groupRepository.findByExternalId(request.getExternalId())
                .orElseGet(() -> {
                    Group g = new Group();
                    g.setExternalId(request.getExternalId());
                    g.setDisplayName(request.getDisplayName());
                    return g;
                });

        // Add members if provided
        if (request.getMembers() != null && !request.getMembers().isEmpty()) {
            Set<User> membersToAdd = request.getMembers().stream()
                    .map(ScimGroupRequest.Member::getValue)
                    .map(userRepository::findByExternalId) // or findByUsername
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            group.getMembers().addAll(membersToAdd);
        }

        return groupRepository.save(group);
    }


    private void handleReplace(Group group, ScimPatchRequest.PatchOperation op) {

        if ("displayName".equals(op.getPath())) {
            group.setDisplayName(op.getValue().toString());
        }
    }

    private String extractUserId(String path) {
        int start = path.indexOf("\"") + 1;
        int end = path.lastIndexOf("\"");
        return path.substring(start, end);
    }

    private void handleRemove(Group group, ScimPatchRequest.PatchOperation op) {

        if (!op.getPath().startsWith("members")) return;

        // members[value eq "USER_ID"]
        String userId = extractUserId(op.getPath());

        group.getMembers().removeIf(u -> u.getId().equals(userId));
    }

    private void handleAdd(Group group, ScimPatchRequest.PatchOperation op) {

        if (!"members".equals(op.getPath())) return;

        List<Map<String, String>> members =
                (List<Map<String, String>>) op.getValue();

        for (var member : members) {
            String userId = member.get("value");

            userRepository.findById(UUID.fromString(userId))
                    .ifPresent(group.getMembers()::add);
        }
    }

    private void applyGroupPatch(Group group, ScimPatchRequest.PatchOperation op) {

        switch (op.getOp().toLowerCase()) {

            case "add" -> handleAdd(group, op);

            case "remove" -> handleRemove(group, op);

            case "replace" -> handleReplace(group, op);

            default -> {
                throw new IllegalArgumentException("Unsupported patch operation: " + op.getOp());
            }
        }
    }

    @Transactional
    public Group patchGroup(String groupId, ScimPatchRequest request) throws ScimNotFoundException {

        Group group = groupRepository.findById(UUID.fromString(groupId))
                .orElseThrow(() -> new ScimNotFoundException("Group not found"));

        for (var op : request.getOperations()) {
            applyGroupPatch(group, op);
        }

        return groupRepository.save(group);
    }

    public List<Group> filterGroups(String filter) {

        if (filter == null) {
            return groupRepository.findAll();
        }

        if (filter.startsWith("externalId")) {
            return ScimFilter.extractEquals(filter)
                    .flatMap(groupRepository::findByExternalId)
                    .map(List::of)
                    .orElse(List.of());
        }

        return List.of();
    }

    public List<User> filterUsers(String filter) {

        if (filter == null) {
            return userRepository.findAll();
        }

        if (filter.startsWith("externalId")) {
            return ScimFilter.extractEquals(filter)
                    .flatMap(userRepository::findByExternalId)
                    .map(List::of)
                    .orElse(List.of());
        }

        if (filter.startsWith("userName")) {
            return ScimFilter.extractEquals(filter)
                    .flatMap(userRepository::findByUsername)
                    .map(List::of)
                    .orElse(List.of());
        }

        return List.of(); // SCIM-compliant empty result
    }
    /**
     * Fetch a group by ID
     */
    public Optional<Group> getGroupById(UUID id) {
        return groupRepository.findById(id);
    }

    /**
     * List groups with optional filter
     */
    public List<Group> listGroups(String filter) {
        List<Group> groups = groupRepository.findAll();

        if (filter == null || filter.isEmpty()) {
            return groups;
        }

        // Example: simple "displayName eq "Admins"" filter
        if (filter.startsWith("displayName eq")) {
            String value = filter.substring(filter.indexOf('"') + 1, filter.lastIndexOf('"'));
            return groups.stream()
                    .filter(g -> g.getDisplayName().equalsIgnoreCase(value))
                    .collect(Collectors.toList());
        }

        if (filter.startsWith("externalId eq")) {
            String value = filter.substring(filter.indexOf('"') + 1, filter.lastIndexOf('"'));
            return groups.stream()
                    .filter(g -> g.getExternalId().equalsIgnoreCase(value))
                    .collect(Collectors.toList());
        }

        // Extend to support other filters
        return groups;
    }

    /**
     * Delete a group
     */
    @Transactional
    public boolean deleteGroup(String id) {
        UUID uuid = UUID.fromString(id);
        if (!groupRepository.existsById(uuid)) return false;
        groupRepository.deleteById(uuid);
        return true;
    }

    /**
     * Add members to a group
     */
    @Transactional
    public Optional<Group> addMembers(String groupId, List<String> memberIds) {
        List<User> allMembersByIdIn = userRepository.findAllByIdIn(memberIds.stream().map(UUID::fromString).collect(Collectors.toSet()));
        Optional<Group> optionalGroup = groupRepository.findById(UUID.fromString(groupId));
        if (optionalGroup.isEmpty()) return Optional.empty();

        Group group = optionalGroup.get();
        Set<User> currentMembers = group.getMembers() != null ? group.getMembers() : new HashSet<>();
        currentMembers.addAll(allMembersByIdIn);
        group.setMembers(currentMembers);

        return Optional.of(groupRepository.save(group));
    }

    /**
     * Remove members from a group
     */
    @Transactional
    public Optional<Group> removeMembers(String groupId, List<String> memberIds) {
        Optional<Group> optionalGroup = groupRepository.findById(UUID.fromString(groupId));
        if (optionalGroup.isEmpty()) return Optional.empty();

        Group group = optionalGroup.get();
        Set<User> currentMembers = group.getMembers() != null ? group.getMembers() : new HashSet<>();
        currentMembers.removeAll(memberIds);
        group.setMembers(currentMembers);

        return Optional.of(groupRepository.save(group));
    }


}
