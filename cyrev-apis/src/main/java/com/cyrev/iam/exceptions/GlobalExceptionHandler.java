package com.cyrev.iam.exceptions;

import com.cyrev.common.dtos.ApiErrorResponse;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowServiceException;
import io.temporal.failure.ActivityFailure;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* =====================
       DOMAIN / BUSINESS
       ===================== */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }
    @ExceptionHandler({BadRequestException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    /* =====================
       TEMPORAL ERRORS
       ===================== */

    @ExceptionHandler(WorkflowServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleTemporalServiceException(
            WorkflowServiceException ex,
            HttpServletRequest request
    ) {
        log.error("Temporal service error", ex);

        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Workflow service is currently unavailable",
                request
        );
    }

    @ExceptionHandler(WorkflowExecutionAlreadyStarted.class)
    public ResponseEntity<ApiErrorResponse> handleWorkflowAlreadyStarted(
            WorkflowExecutionAlreadyStarted ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.CONFLICT,
                "Provisioning already in progress for this user",
                request
        );
    }

    @ExceptionHandler(ActivityFailure.class)
    public ResponseEntity<ApiErrorResponse> handleActivityFailure(
            ActivityFailure ex,
            HttpServletRequest request
    ) {
        log.error("Activity failure", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Provisioning activity failed",
                request
        );
    }

    /* =====================
       VALIDATION ERRORS
       ===================== */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return buildError(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    /* =====================
       FALLBACK
       ===================== */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    /* =====================
       HELPER
       ===================== */

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }
}
