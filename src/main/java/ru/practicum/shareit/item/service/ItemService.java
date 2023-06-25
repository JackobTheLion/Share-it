package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.exceptions.CommentNotAllowedException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Autowired
    public ItemService(ItemStorage itemStorage, UserStorage userStorage,
                       BookingStorage bookingStorage, CommentStorage commentStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
        this.bookingStorage = bookingStorage;
        this.commentStorage = commentStorage;
    }

    public Item addItem(Item item) {
        log.info("Adding item {}", item);
        if (item.getName() == null || item.getName().isEmpty()) {
            log.info("Item name cannot be empty");
            throw new ValidationException("Item name cannot be empty");
        }
        User user = userStorage.findById(item.getOwnerId()).orElseThrow(() -> {
            log.info("Item id {} not found ", item.getId());
            return new ItemNotFoundException(String.format("User id %s not found", item.getOwnerId()));
        });
        itemStorage.save(item);
        log.info("Item added {}.", item);
        return item;
    }

    public Item updateItem(Item item) {
        log.info("Updating item with: {}", item);
        Item savedItem = itemStorage.findById(item.getId()).orElseThrow(() -> {
            log.info("Item id {} not found ", item.getId());
            return new ItemNotFoundException(String.format("Item id %s not found", item.getId()));
        });

        if (!savedItem.getOwnerId().equals(item.getOwnerId())) {
            log.info("Item {} does not belong to user {}.", item.getId(), item.getOwnerId());
            throw new ItemNotFoundException(String.format("Item id %s not found", item.getId()));
        }
        if (item.getName() != null) {
            savedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            savedItem.setDescription(item.getDescription());
        }
        if (item.getIsAvailable() != null) {
            savedItem.setIsAvailable(item.getIsAvailable());
        }
        log.info("Item updated: {}", savedItem);
        return itemStorage.save(savedItem);
    }

    public List<Item> getAllItems(Long userId) {
        List<Item> items;
        if (userId == null) {
            log.info("userId is null. Getting all items");
            items = itemStorage.findAll();
        } else {
            log.info("Getting all items of user id: {}", userId);
            items = itemStorage.findAllByOwnerId(userId);
            setBookingsToItems(items);
        }

        log.info("Number of items found: {}", items.size());
        return items;
    }

    public Item getItem(Long itemId, Long userId) {
        log.info("Looking for item id {} by user {}", itemId, userId);
        Item item = itemStorage.findById(itemId).orElseThrow(() -> {
            log.info("Item id {} not found ", itemId);
            return new ItemNotFoundException(String.format("Item id %s not found", itemId));
        });
        log.info("Item found: {}", item);
        if (item.getOwnerId().equals(userId)) {
            List<Booking> bookings = bookingStorage.findBookingByItemId(itemId);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            item.setLastBooking(getLastBooking(bookings, itemId, now));
            item.setNextBooking(getNextBooking(bookings, itemId, now));
        }
        return item;
    }

    public List<Item> searchItem(String text, Long userId) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        List<Item> items = itemStorage
                .findItemByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailableIsTrue(text, text);

        setBookingsToItems(items);

        log.info("Number of items found: {}", items.size());
        return items;
    }

    public void deleteItem(Long itemId, Long userId) {
        log.info("Deleting item id {} by user id {}", itemId, userId);
        Item savedItem = itemStorage.findById(itemId).orElseThrow(() -> {
            log.info("Item id {} not found ", itemId);
            return new ItemNotFoundException(String.format("Item id %s not found", itemId));
        });

        if (!savedItem.getOwnerId().equals(itemId)) {
            log.info("Item {} does not belong to user {}.", itemId, userId);
            throw new ItemNotFoundException(String.format("Item id %s not found", itemId));
        }
        itemStorage.deleteById(itemId);
    }

    public Comment addComment(Comment comment) {
        log.info("Adding comment {}.", comment);
        Item savedItem = itemStorage.findById(comment.getItem().getId()).orElseThrow(() -> {
            log.info("Item id {} not found ", comment.getItem().getId());
            return new ItemNotFoundException(String.format("Item id %s not found", comment.getItem().getId()));
        });
        comment.setItem(savedItem);

        User user = userStorage.findById(comment.getAuthor().getId()).orElseThrow(() -> {
            log.info("Item id {} not found ", comment.getAuthor().getId());
            return new ItemNotFoundException(String.format("User id %s not found", comment.getAuthor().getId()));
        });
        comment.setAuthor(user);

        List<Booking> bookings = bookingStorage.findByItemIdAndBookerIdAndStatusNotAndStartDateBefore(savedItem.getId(),
                user.getId(), Status.REJECTED, Timestamp.valueOf(LocalDateTime.now()));

        if (bookings.isEmpty()) {
            log.info("User id {} did not book item and cannot leave comment", user.getId());
            throw new CommentNotAllowedException(
                    String.format("User id %s did not book item and cannot leave comment", user.getId()));
        }
        comment.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        Comment savedComment = commentStorage.save(comment);
        log.info("Comment saved: {}", savedComment);
        return savedComment;
    }

    private List<Item> setBookingsToItems(List<Item> items) {
        List<Long> ids = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> bookings = bookingStorage.findBookingByItemIdIn(ids);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (Item i : items) {
            i.setLastBooking(getLastBooking(bookings, i.getId(), now));
            i.setNextBooking(getNextBooking(bookings, i.getId(), now));
            log.info("Item {} last booking {}, next booking {}.", i, i.getLastBooking(), i.getNextBooking());
        }

        return items;
    }

    private Booking getLastBooking(List<Booking> bookings, Long id, Timestamp now) {
        bookings = bookings.stream()
                .filter(booking -> booking.getItem().getId().equals(id))
                .filter(booking -> booking.getStartDate().before(now))
                .filter(booking -> !booking.getStatus().equals(Status.REJECTED))
                .sorted(Comparator.comparing(Booking::getEndDate))
                .collect(Collectors.toList());

        if (bookings.isEmpty()) {
            return null;
        } else return bookings.get(bookings.size() - 1);
    }

    private Booking getNextBooking(List<Booking> bookings, Long id, Timestamp now) {
        bookings = bookings.stream()
                .filter(booking -> booking.getItem().getId().equals(id))
                .filter(booking -> booking.getStartDate().after(now))
                .filter(booking -> !booking.getStatus().equals(Status.REJECTED))
                .sorted(Comparator.comparing(Booking::getStartDate))
                .collect(Collectors.toList());
        if (bookings.isEmpty()) {
            return null;
        } else return bookings.get(0);
    }
}
