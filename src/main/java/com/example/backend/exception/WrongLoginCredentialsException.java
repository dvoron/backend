package com.example.backend.exception;

public class WrongLoginCredentialsException extends RuntimeException {
    public WrongLoginCredentialsException() {
        super("Username, email or password are incorrect");
    }
}
