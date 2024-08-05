package com.osc.userservice.excetion;

public class ForgotPasswordException extends RuntimeException {
    public ForgotPasswordException(String message) {
        super(message);
    }

    public static class UserNotFoundException extends ForgotPasswordException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class OTPProcessingException extends ForgotPasswordException {
        public OTPProcessingException(String message) {
            super(message);
        }
    }
}