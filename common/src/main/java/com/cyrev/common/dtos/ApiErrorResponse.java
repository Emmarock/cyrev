package com.cyrev.common.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {
    private Instant timestamp;
    private int status;
    private String message;
    private Object details;
    private String path;
}