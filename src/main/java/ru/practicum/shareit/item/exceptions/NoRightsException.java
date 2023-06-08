package ru.practicum.shareit.item.exceptions;

public class NoRightsException extends RuntimeException {
    public NoRightsException() {
    }

    public NoRightsException(String message) {
        super(message);
    }

    public NoRightsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRightsException(Throwable cause) {
        super(cause);
    }
}
