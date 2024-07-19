package com.osc.userservice.excetion;

public class LogoutException {
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class SessionIdMismatchException extends RuntimeException {
        public SessionIdMismatchException(String message) {
            super(message);
        }
    }

    public static class SessionDataProcessingException extends RuntimeException {
        public SessionDataProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}
