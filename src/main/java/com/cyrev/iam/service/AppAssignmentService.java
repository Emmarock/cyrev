package com.cyrev.iam.service;

import com.cyrev.iam.adapters.AppProvisioningAdapter;
import com.cyrev.iam.approval.ApprovalPolicy;
import com.cyrev.iam.domain.*;
import com.cyrev.iam.temporal.workflow.AppProvisioningWorkflowStarter;
import com.cyrev.iam.entities.AppAssignment;
import com.cyrev.iam.entities.User;
import com.cyrev.iam.repository.AppAssignmentRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppAssignmentService {

    private final AppAssignmentRepository assignmentRepository;
    private final Map<App, AppProvisioningAdapter> adapters;
    private final UserService userService;
    private final ApprovalPolicy approvalPolicy;
    private final AppProvisioningWorkflowStarter workflowStarter;

    public AppAssignmentService(AppAssignmentRepository assignmentRepository, List<AppProvisioningAdapter> adapters, UserService userService, ApprovalPolicy approvalPolicy, AppProvisioningWorkflowStarter workflowStarter) {
        this.assignmentRepository = assignmentRepository;
        this.adapters = adapters.stream()
                .collect(Collectors.toMap(AppProvisioningAdapter::app, a -> a));
        this.userService = userService;
        this.approvalPolicy = approvalPolicy;
        this.workflowStarter = workflowStarter;
    }

    public List<AppAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public List<AppAssignment> getAssignmentsByUser(UUID userId) {
        return assignmentRepository.findByUserId(userId);
    }

    public List<AppAssignment> getAssignmentsByManager(UUID managerId) {
        return assignmentRepository.findByManager_Id(managerId);
    }

    public void requestAppAccess(UUID userId, AppAssignmentRequestDTO assignment) throws BadRequestException {
        User currentUser = userService.getUser(userId);
        Map<App, Role> appRoleHashMap = new HashMap<>();
        Map<App, Role> appRoleMap = assignment.getAppsRole();

        for (Map.Entry<App, Role> entry : appRoleMap.entrySet()) {
            App app = entry.getKey();
            Role role = entry.getValue();
            boolean requiresApproval = approvalPolicy.requiresApproval(app, role );
            Optional<AppAssignment> optionalAppAssignment = assignmentRepository.findByAppAndRoleAndUserId(app, role, userId);
            if (!requiresApproval & optionalAppAssignment.isEmpty()) {
                adapters.get(app).assignUser(currentUser, role);

            }
            else if(requiresApproval && optionalAppAssignment.isEmpty()){
                appRoleHashMap.put(app, role);
            }
        }


        workflowStarter.startProvisioning(currentUser.getId(),appRoleHashMap);
    }

    public void approveAppProvisioningRequest(AppApprovalRequestDTO assignment) throws BadRequestException {
        validateApprovalOrRejectRequest(assignment, "Invalid approval request, only the user's manager can approve this request");
        workflowStarter.approveProvisioningRequest(assignment.getApprovalId(),assignment.getUserId());
    }

    public void rejectAppProvisioningRequest(AppApprovalRequestDTO assignment) throws BadRequestException {
        validateApprovalOrRejectRequest(assignment, "Invalid approval request, only the user's manager can reject this request");
        workflowStarter.rejectProvisioningRequest(assignment.getApprovalId(),assignment.getUserId(), assignment.getRejectionReason());
    }

    private void validateApprovalOrRejectRequest(AppApprovalRequestDTO assignment, String message) throws BadRequestException {
        if (assignment.getApprovalId() == null) {
            throw new BadRequestException("Invalid approval request");
        }

        User user = userService.getUser(assignment.getUserId());
        User manager = user.getManager();
        if(manager == null) {
            throw new BadRequestException("Invalid user, ensure this user has an assigned manager");
        }
        UUID managerId = manager.getId();
        if (assignment.getApprovalId() == null || managerId == null || !managerId.equals(assignment.getApprovalId())) {
            throw new BadRequestException(message);
        }
    }

    public ProvisioningState getState(UUID userId){
        return workflowStarter.getState(userId);
    }

    public AppAssignment updateAssignment(String id, AppAssignment updated) {
        return assignmentRepository.findById(id).map(existing -> {
            existing.setApp(updated.getApp());
            existing.setRole(updated.getRole());
            existing.setStatus(updated.getStatus());
            return assignmentRepository.save(existing);
        }).orElseThrow(()-> new RuntimeException("Invalid user, ensure this user has an assigned manager"));
    }

    public boolean deleteAssignment(String id) {
        return assignmentRepository.findById(id).map(existing -> {
            assignmentRepository.delete(existing);
            return true;
        }).orElse(false);
    }
}
