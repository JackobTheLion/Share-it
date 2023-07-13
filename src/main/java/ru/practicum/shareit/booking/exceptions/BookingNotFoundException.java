package ru.practicum.shareit.booking.exceptions;

import ru.practicum.shareit.exceptions.NotFoundException;

public class BookingNotFoundException extends NotFoundException {

    public BookingNotFoundException(String message) {
        super(message);
    }

}
