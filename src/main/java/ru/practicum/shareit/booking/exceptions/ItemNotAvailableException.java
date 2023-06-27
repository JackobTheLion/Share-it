package ru.practicum.shareit.booking.exceptions;

public class ItemNotAvailableException extends RuntimeException {

    public ItemNotAvailableException() {
    }

    public ItemNotAvailableException(String message) {
        super(message);
    }

    public ItemNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemNotAvailableException(Throwable cause) {
        super(cause);
    }
}
