package com.bin.Exception;


import org.springframework.security.core.AuthenticationException;

public class NonTokenException extends AuthenticationException {
    public NonTokenException(String msg) {
        super(msg);
    }
}
