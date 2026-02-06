package com.cyrev.iam.controllers;

import com.cyrev.iam.service.ReconciliationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/entra")
    public String reconcileWithEntra() {
        int reconciledCount = reconciliationService.reconcileWithEntra();
        return "Reconciliation completed. Total reconciled: " + reconciledCount;
    }
}
