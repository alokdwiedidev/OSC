package com.osc.userservice.excetion;

    public class PasswordException {

        public static class UserNotFoundException extends RuntimeException {
            public UserNotFoundException(String message) {
                super(message);
            }
        }

        public static class PasswordCreationException extends RuntimeException {
            public PasswordCreationException(String message) {
                super(message);
            }
        }
    }

