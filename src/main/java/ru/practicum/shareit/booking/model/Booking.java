package ru.practicum.shareit.booking.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class Booking {
    private final Long itemId;
    private final LocalDate bookingStartDate;
    private final LocalDate bookingEndDate;
    private boolean isConfirmed;
}
