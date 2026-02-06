package com.cyrev.iam.rbac;

import com.cyrev.common.dtos.Action;
import com.cyrev.common.dtos.App;
import com.cyrev.common.dtos.SystemRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class RbacService {

 public void checkPermission(SystemRole role, Action action, App app) {

  if (role == SystemRole.ORG_ADMIN) return;

  if (role == SystemRole.IT_ADMIN && action == Action.ASSIGN) return;

  if (role == SystemRole.MANAGER && action == Action.APPROVE) return;

  throw new AccessDeniedException("Not allowed");
 }
}

