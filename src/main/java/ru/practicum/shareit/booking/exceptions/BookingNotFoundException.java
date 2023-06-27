package ru.practicum.shareit.booking.exceptions;

import ru.practicum.shareit.exceptions.NotFoundException;

public class BookingNotFoundException extends NotFoundException {

    public BookingNotFoundException() {
    }

    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookingNotFoundException(Throwable cause) {
        super(cause);
    }
}
