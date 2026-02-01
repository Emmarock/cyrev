package com.cyrev.iam.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CyrevApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
