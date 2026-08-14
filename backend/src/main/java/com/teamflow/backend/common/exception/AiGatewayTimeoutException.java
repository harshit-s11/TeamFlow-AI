package com.teamflow.backend.common.exception;

public class AiGatewayTimeoutException extends RuntimeException {
    public AiGatewayTimeoutException(String message) {
        super(message);
    }
}
