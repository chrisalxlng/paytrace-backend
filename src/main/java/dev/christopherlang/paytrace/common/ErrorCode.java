package dev.christopherlang.paytrace.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    UNSUPPORTED_PROVIDER(
            "UNSUPPORTED_PROVIDER",
            "The provider of this payroll file is not supported.",
            HttpStatus.BAD_REQUEST
    ),

    PAYROLL_PARSING_FAILED(
            "PAYROLL_PARSING_FAILED",
            "Could not extract data from the PDF file.",
            HttpStatus.BAD_REQUEST
    ),

    DUPLICATE_PAYROLL_ENTRY(
            "DUPLICATE_PAYROLL_ENTRY",
            "Only one entry per type is allowed.",
            HttpStatus.BAD_REQUEST
    ),

    DUPLICATE_ACCOUNTING_PERIOD(
            "DUPLICATE_ACCOUNTING_PERIOD",
            "A payroll entry for this accounting period already exists.",
            HttpStatus.BAD_REQUEST
    ),

    VALIDATION_ERROR(
            "VALIDATION_ERROR",
            "Invalid request data.",
            HttpStatus.BAD_REQUEST
    ),

    MISSING_PARAMETER(
            "MISSING_PARAMETER",
            "Required request parameter is missing.",
            HttpStatus.BAD_REQUEST
    ),

    TYPE_MISMATCH(
            "TYPE_MISMATCH",
            "Invalid parameter type.",
            HttpStatus.BAD_REQUEST
    ),

    MALFORMED_JSON(
            "MALFORMED_JSON",
            "Malformed JSON request.",
            HttpStatus.BAD_REQUEST
    ),

    FORBIDDEN(
            "FORBIDDEN",
            "Access denied.",
            HttpStatus.FORBIDDEN
    ),

    UNAUTHORIZED(
            "UNAUTHORIZED",
            "Authentication required.",
            HttpStatus.UNAUTHORIZED
    ),

    DATABASE_ERROR(
            "DATABASE_ERROR",
            "A database error occurred.",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    NOT_FOUND(
            "NOT_FOUND",
            "Resource not found.",
            HttpStatus.NOT_FOUND
    ),

    INTERNAL_SERVER_ERROR(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred.",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(String code, String defaultMessage, HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
    public HttpStatus getStatus() { return status; }

}
