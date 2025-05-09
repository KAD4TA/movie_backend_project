package com.film_backend.film.dtos.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private String error;
    private String errorCode;
    private int status;
    private String timestamp;
    private Map<String, String> details;
}
