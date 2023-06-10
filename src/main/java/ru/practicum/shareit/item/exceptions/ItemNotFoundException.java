package ru.practicum.shareit.item.exceptions;

import ru.practicum.shareit.exceptions.NotFoundException;

public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException() {
    }

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemNotFoundException(Throwable cause) {
        super(cause);
    }
}
