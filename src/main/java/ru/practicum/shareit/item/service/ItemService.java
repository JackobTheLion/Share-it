package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.exceptions.CommentNotAllowedException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository,
                       BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    public Item addItem(Item item) {
        log.info("Adding item {}", item);
        User user = userRepository.findById(item.getOwnerId()).orElseThrow(() -> {
            log.info("Item id {} not found ", item.getId());
            return new ItemNotFoundException(String.format("User id %s not found", item.getOwnerId()));
        });
        Item savedItem = itemRepository.save(item);
        log.info("Item added {}.", item);
        return savedItem;
    }

    public Item updateItem(Item item) {
        log.info("Updating item with: {}", item);
        Item savedItem = itemRepository.findById(item.getId()).orElseThrow(() -> {
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
        return itemRepository.save(savedItem);
    }

    public List<Item> getAllItems(Long userId, int from, int size) {
        Page<Item> items;
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        if (userId == null) {
            log.info("userId is null. Getting all items");
            items = itemRepository.findAll(page);
        } else {
            log.info("Getting all items of user id: {}", userId);
            items = itemRepository.findAllByOwnerId(userId, page);
            setBookingsToItems(items.getContent());
        }

        log.info("Number of items found: {}", items);
        return items.getContent();
    }

    public Item getItem(Long itemId, Long userId) {
        log.info("Looking for item id {} by user {}", itemId, userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.info("Item id {} not found ", itemId);
            return new ItemNotFoundException(String.format("Item id %s not found", itemId));
        });
        log.info("Item found: {}", item);
        if (item.getOwnerId().equals(userId)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            item.setLastBooking(getLastBooking(itemId, now));
            item.setNextBooking(getNextBooking(itemId, now));
        }
        return item;
    }

    public List<Item> searchItem(String text, Long userId, int from, int size) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Item> items = itemRepository
                .findItemByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailableIsTrue(text, text, page);

        setBookingsToItems(items.getContent());

        log.info("Number of items found: {}", items);
        return items.getContent();
    }

    public void deleteItem(Long itemId, Long userId) {
        log.info("Deleting item id {} by user id {}", itemId, userId);
        Item savedItem = itemRepository.findById(itemId).orElseThrow(() -> {
            log.info("Item id {} not found ", itemId);
            return new ItemNotFoundException(String.format("Item id %s not found", itemId));
        });

        if (!savedItem.getOwnerId().equals(itemId)) {
            log.info("Item {} does not belong to user {}.", itemId, userId);
            throw new ItemNotFoundException(String.format("Item id %s not found", itemId));
        }
        itemRepository.deleteById(itemId);
    }

    public Comment addComment(Comment comment) {
        log.info("Adding comment {}.", comment);
        Item savedItem = itemRepository.findById(comment.getItem().getId()).orElseThrow(() -> {
            log.info("Item id {} not found ", comment.getItem().getId());
            return new ItemNotFoundException(String.format("Item id %s not found", comment.getItem().getId()));
        });
        comment.setItem(savedItem);

        User user = userRepository.findById(comment.getAuthor().getId()).orElseThrow(() -> {
            log.info("Item id {} not found ", comment.getAuthor().getId());
            return new ItemNotFoundException(String.format("User id %s not found", comment.getAuthor().getId()));
        });
        comment.setAuthor(user);

        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatusNotAndStartDateBefore(savedItem.getId(),
                user.getId(), Status.REJECTED, Timestamp.valueOf(LocalDateTime.now()));

        if (bookings.isEmpty()) {
            log.info("User id {} did not book item and cannot leave comment", user.getId());
            throw new CommentNotAllowedException(
                    String.format("User id %s did not book item and cannot leave comment", user.getId()));
        }
        comment.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment saved: {}", savedComment);
        return savedComment;
    }

    private List<Item> setBookingsToItems(List<Item> items) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (Item i : items) {
            i.setLastBooking(getLastBooking(i.getId(), now));
            i.setNextBooking(getNextBooking(i.getId(), now));
            log.info("Item {} last booking {}, next booking {}.", i, i.getLastBooking(), i.getNextBooking());
        }
        return items;
    }

    private Booking getLastBooking(Long itemId, Timestamp now) {
        List<Booking> b = bookingRepository.findLastBooking(itemId, now);
        log.info("Bookings found: {}", b);
        if (!b.isEmpty()) {
            return b.get(0);
        }
        return null;
    }

    private Booking getNextBooking(Long itemId, Timestamp now) {
        List<Booking> b = bookingRepository.findNextBooking(itemId, now);
        log.info("Bookings found: {}", b);
        if (!b.isEmpty()) {
            return b.get(0);
        }
        return null;
    }
}