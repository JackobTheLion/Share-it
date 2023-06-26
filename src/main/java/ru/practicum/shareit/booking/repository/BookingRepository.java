package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.sql.Timestamp;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
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

    List<Booking> findByItemIdAndBookerIdAndStatusNotAndStartDateBefore(Long itemId, Long bookerId, Status status, Timestamp timestamp);

    @Query(value = "select * from bookings where " +
            "item_id = ?1 and " +
            "status <> 'REJECTED' and " +
            "start_date < ?2 " +
            "order by start_date desc limit 1", nativeQuery = true)
    List<Booking> findLastBooking(Long itemId, Timestamp timestamp);

    @Query(value = "select * from bookings where " +
            "item_id = ?1 and " +
            "status <> 'REJECTED' and " +
            "start_date > ?2 " +
            "order by start_date asc limit 1", nativeQuery = true)
    List<Booking> findNextBooking(Long itemId, Timestamp timestamp);
}