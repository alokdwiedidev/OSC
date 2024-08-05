package com.osc.userservice.excetion;

public class LoginException {


    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class IncorrectPasswordException extends RuntimeException {
        public IncorrectPasswordException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyLoggedInException extends RuntimeException {
        public UserAlreadyLoggedInException(String message) {
            super(message);
        }
    }

    public static class SessionDataProcessingException extends RuntimeException {
        public SessionDataProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException(String message) {
            super(message);
        }
    }
}


