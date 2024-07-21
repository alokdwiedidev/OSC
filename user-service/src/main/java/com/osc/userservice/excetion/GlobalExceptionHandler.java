package com.osc.userservice.excetion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.Api;
import com.osc.userservice.responce.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(JsonProcessingCustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleJsonProcessingCustomException(JsonProcessingCustomException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleExistingUser(UserAlreadyExistsException ex) {
        log.error("OTP validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(409, null));
    }

    @ExceptionHandler(OtpExceptions.OtpValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleOtpValidationException(OtpExceptions.OtpValidationException ex) {
        log.error("OTP validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(502, null));
    }

    @ExceptionHandler(OtpExceptions.MaximumOtpAttemptsExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaximumOtpAttemptsExceededException(OtpExceptions.MaximumOtpAttemptsExceededException ex) {
        log.error("Maximum OTP attempts exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiResponse<>(301, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(0, null));
    }

    @ExceptionHandler(PasswordException.UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(PasswordException.UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(0, null));
    }

    @ExceptionHandler(PasswordException.PasswordCreationException.class)
    public ResponseEntity<ApiResponse<Object>> handlePasswordCreationException(PasswordException.PasswordCreationException ex) {
        log.error("Password creation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(0, null));
    }

    @ExceptionHandler(LoginException.IncorrectPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handleIncorrectPasswordException(LoginException.IncorrectPasswordException ex) {
        log.error("Incorrect password: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(202, null));

    }

    @ExceptionHandler(LoginException.UserAlreadyLoggedInException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyLoggedInException(LoginException.UserAlreadyLoggedInException ex) {
        log.error("User already logged in: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(204, null));
    }

    @ExceptionHandler(LoginException.SessionDataProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleSessionDataProcessingException(LoginException.SessionDataProcessingException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
    }

    @ExceptionHandler(LogoutException.UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(LogoutException.UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(0, null));
    }

    @ExceptionHandler(LogoutException.SessionIdMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleSessionIdMismatchException(LogoutException.SessionIdMismatchException ex) {
        log.error("Session ID mismatch: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(0, null));
    }

    @ExceptionHandler(LogoutException.SessionDataProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleSessionDataProcessingException(LogoutException.SessionDataProcessingException ex) {
        log.error("Session data processing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(0, null));
    }

    @ExceptionHandler(ForgotPasswordException.UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(ForgotPasswordException.UserNotFoundException ex, WebRequest request) {
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body( new ApiResponse<>(199, ex.getMessage()));

    }

    @ExceptionHandler(ForgotPasswordException.OTPProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleOTPProcessingException(ForgotPasswordException.OTPProcessingException ex, WebRequest request) {
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body( new ApiResponse<>(199,null));

    }
}
