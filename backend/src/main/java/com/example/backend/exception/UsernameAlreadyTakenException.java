package com.example.backend.exception;

public class UsernameAlreadyTakenException extends RuntimeException {
    public UsernameAlreadyTakenException(String username) {
        super("This username is already taken: " + username);
    }
}
