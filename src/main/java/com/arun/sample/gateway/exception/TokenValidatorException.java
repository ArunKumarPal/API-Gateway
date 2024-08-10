package com.arun.sample.gateway.exception;

import com.arun.sample.gateway.constants.ErrorCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TokenValidatorException extends ApiGatewayException {
    public TokenValidatorException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TokenValidatorException(Throwable exception) {
        super(exception);
    }
}
