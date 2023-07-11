package ru.practicum.shareit.request.exceptions;

import ru.practicum.shareit.exceptions.NotFoundException;

public class RequestNotFoundException extends NotFoundException {

    public RequestNotFoundException(String message) {
        super(message);
    }

}
