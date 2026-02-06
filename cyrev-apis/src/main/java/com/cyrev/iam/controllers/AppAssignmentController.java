package com.cyrev.iam.controllers;

import com.cyrev.common.dtos.*;
import com.cyrev.iam.annotations.CurrentUserId;
import com.cyrev.common.entities.AppAssignment;
import com.cyrev.iam.service.AppAssignmentService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
public class AppAssignmentController {

    private final AppAssignmentService assignmentService;

    public AppAssignmentController(AppAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<CyrevApiResponse<List<AppAssignment>>> getAllAssignments() {
        var response =  assignmentService.getAllAssignments();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "App assignments retrieved successfully",
                        response
                ));
    }

    @GetMapping("/user")
    public ResponseEntity<CyrevApiResponse<List<AppAssignment>>> getAssignmentsByUser(@CurrentUserId UUID userId) {
        var response =  assignmentService.getAssignmentsByUser(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "App assignments retrieved successfully",
                        response
                ));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<CyrevApiResponse<List<AppAssignment>>> getAssignmentsByManager(@PathVariable UUID managerId) {
        var response =  assignmentService.getAssignmentsByManager(managerId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "Manager's app assignments retrieved successfully",
                        response
                ));
    }
    @GetMapping("/status")
    public ResponseEntity<CyrevApiResponse<ProvisioningResponse>> status(@CurrentUserId UUID userId) {
        ProvisioningState state = assignmentService.getState(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CyrevApiResponse<>(
                        true,
                        "App workflow state retrieved",
                        ProvisioningResponse.builder().workflowId("app-user-"+userId).message(state.toString()).build()
                ));
    }

    @PostMapping("/request")
    public ResponseEntity<CyrevApiResponse<Void>> requestAppAccess(@CurrentUserId UUID userId, @RequestBody AppAssignmentRequestDTO assignment) throws BadRequestException {
        assignmentService.requestAppAccess(userId, assignment);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "App access requested successfully",
                        null
                ));
    }
    @PostMapping("/approve")
    public ResponseEntity<CyrevApiResponse<Void>> approveAppProvisioningRequest(@RequestBody AppApprovalRequestDTO assignment) throws BadRequestException {
        assignmentService.approveAppProvisioningRequest(assignment);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "App access approved successfully",
                        null
                ));
    }
    @PostMapping("/reject")
    public ResponseEntity<CyrevApiResponse<Void>>  rejectAppProvisioningRequest(@RequestBody AppApprovalRequestDTO assignment) throws BadRequestException {
        assignmentService.rejectAppProvisioningRequest(assignment);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "App access rejected successfully",
                        null
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CyrevApiResponse<AppAssignment>> updateAssignment(@PathVariable String id,
                                                                            @RequestBody AppAssignment updated) {
        AppAssignment appAssignment = assignmentService.updateAssignment(id, updated);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CyrevApiResponse<>(
                        true,
                        "App access rejected successfully",
                        appAssignment
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CyrevApiResponse<Void>> deleteAssignment(@PathVariable String id) {
        return assignmentService.deleteAssignment(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
