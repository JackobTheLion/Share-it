package ru.practicum.shareit.booking.exceptions;

public class BookingNotAloudException extends RuntimeException {

    public BookingNotAloudException() {
    }

    public BookingNotAloudException(String message) {
        super(message);
    }

    public BookingNotAloudException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookingNotAloudException(Throwable cause) {
        super(cause);
    }
}
