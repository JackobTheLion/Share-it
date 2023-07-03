package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.sql.Timestamp;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerIdOrderByStartDateDesc(Long userId, Pageable page);

    Page<Booking> findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long userId, Timestamp t1,
                                                                                      Timestamp t2, Pageable page);

    Page<Booking> findByBookerIdAndEndDateBeforeOrderByStartDateDesc(Long userId, Timestamp t1, Pageable page);

    Page<Booking> findByBookerIdAndStartDateAfterOrderByStartDateDesc(Long userId, Timestamp t1, Pageable page);

    Page<Booking> findByBookerIdAndStatusEqualsOrderByStartDateDesc(Long userId, Status status, Pageable page);

    Page<Booking> findByItemOwnerIdOrderByStartDateDesc(Long userId, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long userId, Timestamp t1,
                                                                                         Timestamp t2, Pageable page);

    Page<Booking> findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(Long userId, Timestamp t1, Pageable page);

    Page<Booking> findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(Long userId, Timestamp t1, Pageable page);

    Page<Booking> findByItemOwnerIdAndStatusEqualsOrderByStartDateDesc(Long userId, Status status, Pageable page);

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