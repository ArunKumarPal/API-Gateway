package com.arun.sample.gateway.exception;

import com.arun.sample.gateway.constants.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiGatewayException extends RuntimeException {
    protected ErrorCode errorCode;

    public ApiGatewayException(Throwable ex) {
        super(ex);
    }

    public ApiGatewayException(ErrorCode code) {
        super(code.getErrMsg());
        this.errorCode = code;
    }
}
