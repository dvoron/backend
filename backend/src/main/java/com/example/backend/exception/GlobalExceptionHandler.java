package com.example.backend.exception;

import com.example.backend.model.dto.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.lang.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex) {
        log.warn("Account locked exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("ACCOUNT_LOCKED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(org.springframework.web.server.ResponseStatusException ex) {
        log.warn("Response status exception: {}", ex.getMessage());
        String code = "ERROR";
        if (ex.getStatusCode() instanceof HttpStatus status) {
            code = status.name();
        }
        ErrorResponse error = new ErrorResponse(code, ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found exception: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Validation exception: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse(ex.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.warn("Spring MVC exception: {}", ex.getMessage());
        String code = "ERROR";
        if (statusCode instanceof HttpStatus status) {
            code = status.name();
        }
        ErrorResponse error = new ErrorResponse(code, ex.getMessage());
        return ResponseEntity.status(statusCode).headers(headers).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}