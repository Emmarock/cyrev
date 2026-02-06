package com.cyrev.iam.scim.controllers;

import com.cyrev.common.entities.Group;
import com.cyrev.iam.exceptions.ScimNotFoundException;
import com.cyrev.iam.scim.dto.*;
import com.cyrev.iam.scim.service.ScimGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/scim/v2/Groups")
@RequiredArgsConstructor
public class ScimGroupController {

    private final ScimGroupService scimGroupService;
    private final ScimGroupMapper scimGroupMapper;
    @PostMapping()
    public ResponseEntity<ScimGroupResponse> createGroup(
            @RequestBody ScimGroupRequest request) {

        Group group = scimGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scimGroupMapper.toScim(group));
    }
    /**
     * Get a group by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable UUID id) {
        Optional<Group> group = scimGroupService.getGroupById(id);
        return group.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * List groups with optional filtering (SCIM filter syntax)
     */
    @GetMapping
    public ResponseEntity<List<Group>> listGroups(@RequestParam(required = false) String filter) {
        List<Group> groups = scimGroupService.listGroups(filter);
        return ResponseEntity.ok(groups);
    }

    /**
     * Patch a group (e.g., add/remove members or update displayName)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Group> patchGroup(@PathVariable String id, @RequestBody ScimPatchRequest patchRequest) throws ScimNotFoundException {
        Optional<Group> updatedGroup = Optional.ofNullable(scimGroupService.patchGroup(id, patchRequest));
        return updatedGroup.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a group
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        boolean deleted = scimGroupService.deleteGroup(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Add members to a group
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<Group> addMembers(@PathVariable String id, @RequestBody List<String> memberIds) {
        Optional<Group> group = scimGroupService.addMembers(id, memberIds);
        return group.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Remove members from a group
     */
    @DeleteMapping("/{id}/members")
    public ResponseEntity<Group> removeMembers(@PathVariable String id, @RequestBody List<String> memberIds) {
        Optional<Group> group = scimGroupService.removeMembers(id, memberIds);
        return group.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
