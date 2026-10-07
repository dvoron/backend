package com.example.backend.exception;

import com.example.backend.model.dto.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> handleUsernameTaken(UsernameAlreadyTakenException ex) {
        log.warn("Username already taken exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("USERNAME_ALREADY_TAKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> handleEmailTaken(EmailAlreadyTakenException ex) {
        log.warn("Email already taken exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("EMAIL_ALREADY_TAKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(WrongLoginCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleWrongLoginCredentials(WrongLoginCredentialsException ex) {
        log.warn("Wrong login credentials exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("WRONG_CREDENTIALS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        log.warn("Invalid refresh token exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("INVALID_REFRESH_TOKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MissingRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleMissingRefreshToken(MissingRefreshTokenException ex) {
        log.warn("Missing refresh token exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("MISSING_REFRESH_TOKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}