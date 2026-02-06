package com.cyrev.iam.approval;

import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.Role;
import org.springframework.stereotype.Component;

@Component
public class ApprovalPolicy {

    public boolean requiresApproval(App app, Role role) {
        if(role==Role.READ){
            return false;
        }
        if (app == App.JIRA && (role == Role.WRITE || role == Role.ADMIN)) {
            return true;
        }
        if (app == App.BITBUCKET && (role == Role.WRITE || role == Role.ADMIN)) {
            return true;
        }
        if (app == App.SLACK && (role == Role.WRITE||role==Role.ADMIN)){
            return true;
        }
        return false;
    }
}
