package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDtoItem;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.sql.Timestamp;

public class BookingMapper {
    public static Booking mapFromDto(BookingRequestDto bookingRequestDto, Long bookerId) {
        Booking booking = Booking.builder()
                .startDate(Timestamp.valueOf(bookingRequestDto.getStart()))
                .endDate(Timestamp.valueOf(bookingRequestDto.getEnd()))
                .item(new Item())
                .booker(new User())
                .build();
        booking.getItem().setId(bookingRequestDto.getItemId());
        booking.getBooker().setId(bookerId);
        return booking;
    }

    public static Booking mapFromDto(BookingRequestDto bookingRequestDto, Long bookerId, Status status) {
        Booking booking = Booking.builder()
                .startDate(Timestamp.valueOf(bookingRequestDto.getStart()))
                .endDate(Timestamp.valueOf(bookingRequestDto.getEnd()))
                .item(new Item())
                .booker(new User())
                .status(status)
                .build();
        booking.getItem().setId(bookingRequestDto.getItemId());
        booking.getBooker().setId(bookerId);
        return booking;
    }

    public static BookingResponseDto mapToDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate().toLocalDateTime())
                .end(booking.getEndDate().toLocalDateTime())
                .item(ItemMapper.mapToDto(booking.getItem()))
                .booker(UserMapper.mapToDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public static BookingResponseDto mapToDto(Booking booking, User user, Item item) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate().toLocalDateTime())
                .end(booking.getEndDate().toLocalDateTime())
                .item(ItemMapper.mapToDto(item))
                .booker(UserMapper.mapToDto(user))
                .status(booking.getStatus())
                .build();
    }

    public static BookingDtoItem mapToDtoItem(Booking booking) {
        return BookingDtoItem.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}