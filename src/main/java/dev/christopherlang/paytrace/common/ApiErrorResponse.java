package dev.christopherlang.paytrace.common;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    String errorCode,
    String message,
    LocalDateTime timestamp
) {}
