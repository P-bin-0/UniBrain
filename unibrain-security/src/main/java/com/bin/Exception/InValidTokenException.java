package com.bin.Exception;


import org.springframework.security.core.AuthenticationException;

public class InValidTokenException extends AuthenticationException {
    public InValidTokenException(String message) {
        super(message);
    }
}
