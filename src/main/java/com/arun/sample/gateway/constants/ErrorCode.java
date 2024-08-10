package com.arun.sample.gateway.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ERR_1001(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "1001", "Unauthorized"),
    ERR_1002(HttpStatus.FORBIDDEN, "FORBIDDEN", "1002", "You do not have permission to access this API"),
    ERR_1003(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "1003", "Issue with internal services. Please try again after sometime"),
    ERR_1004(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "DIS-1004", "User is missing from Bearer Token"),
    ERR_1005(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "1004", "Rate limit exceeded.");

    private final HttpStatus statusCode;
    private final String status;
    private final String errCode;
    private final String errMsg;

    private ErrorCode(HttpStatus statusCode, String status, String errCode, String errMsg) {
        this.statusCode = statusCode;
        this.status = status;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public String createResponseBody() {
        return String.format(
                "{\"errors\": [{\"status\": \"%s\", \"errorMessage\": \"%s (%s)\"}]}",
                this.status,
                this.errMsg,
                this.errCode
        );
    }

}
