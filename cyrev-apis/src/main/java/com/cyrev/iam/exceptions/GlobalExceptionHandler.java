package com.cyrev.iam.exceptions;

import com.cyrev.common.dtos.ApiErrorResponse;
import com.cyrev.common.dtos.ErrorMessageParser;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
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
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Access Denied ", ex);

        return buildError(
                HttpStatus.FORBIDDEN,
                ex, // 👈 include structured JSON
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
                ex,
                request
        );
    }

    /* =====================
   SERVER ERROR
   ===================== */

    @ExceptionHandler({NullPointerException.class, DataAccessException.class, SQLException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationError(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "unable to process request",
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
    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorMessageParser.ParsedError parsedError =
                ErrorMessageParser.parse(ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                parsedError.getMessage(),
                parsedError.getDetails(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }
}
