package ru.practicum.shareit.request.exceptions;

import ru.practicum.shareit.exceptions.NotFoundException;

public class RequestNotFoundException extends NotFoundException {
    public RequestNotFoundException() {
    }

    public RequestNotFoundException(String message) {
        super(message);
    }

    public RequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestNotFoundException(Throwable cause) {
        super(cause);
    }
}
