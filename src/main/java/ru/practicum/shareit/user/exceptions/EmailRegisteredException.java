package ru.practicum.shareit.user.exceptions;

public class EmailRegisteredException extends RuntimeException {
    public EmailRegisteredException() {
    }

    public EmailRegisteredException(String message) {
        super(message);
    }

    public EmailRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailRegisteredException(Throwable cause) {
        super(cause);
    }
}
