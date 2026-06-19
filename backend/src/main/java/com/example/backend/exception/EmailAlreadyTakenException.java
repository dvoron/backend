package com.example.backend.exception;

public class EmailAlreadyTakenException extends RuntimeException{
    public EmailAlreadyTakenException(String email) {
        super("This email is already in use: " + email);
    }
}
