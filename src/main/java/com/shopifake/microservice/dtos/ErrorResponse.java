package com.shopifake.microservice.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Standard error body for the inventory API.
 */
@Value
@Builder
public class ErrorResponse {

    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
}


