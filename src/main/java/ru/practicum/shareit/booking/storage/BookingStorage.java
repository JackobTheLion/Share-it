package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.sql.Timestamp;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDateDesc(Long userId);

    List<Booking> findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long userId, Timestamp t1, Timestamp t2);

    List<Booking> findByBookerIdAndEndDateBeforeOrderByStartDateDesc(Long userId, Timestamp t1);

    List<Booking> findByBookerIdAndStartDateAfterOrderByStartDateDesc(Long userId, Timestamp t1);

    List<Booking> findByBookerIdAndStatusEqualsOrderByStartDateDesc(Long userId, Status status);

    List<Booking> findByItemOwnerIdOrderByStartDateDesc(Long userId);

    List<Booking> findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long userId, Timestamp t1, Timestamp t2);

    List<Booking> findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(Long userId, Timestamp t1);

    List<Booking> findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(Long userId, Timestamp t1);

    List<Booking> findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(Long userId, Status status);

    List<Booking> findByItemId(Long itemId);

    List<Booking> findBookingByItemIdIn(List<Long> itemId);

    List<Booking> findBookingByItemId(Long itemId);

    List<Booking> findByItemIdAndBookerIdAndStatusNotAndStartDateBefore(Long itemId, Long bookerId, Status status, Timestamp timestamp);

}