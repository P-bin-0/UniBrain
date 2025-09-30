package com.bin.Exception;


import org.springframework.security.core.AuthenticationException;

public class UserNonLoginException extends AuthenticationException {
    public UserNonLoginException(String message) {
        super(message);
    }
}
