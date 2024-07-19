package com.osc.userservice.excetion;

public class OtpExceptions {

    public static class OtpValidationException extends RuntimeException {
        public OtpValidationException(String message) {
            super(message);
        }
    }

    public static class MaximumOtpAttemptsExceededException extends RuntimeException {
        public MaximumOtpAttemptsExceededException(String message) {
            super(message);
        }
    }
}

