package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.exceptions.CommentNotAllowedException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ItemService itemService;

    private Item itemToSave;
    private Item itemToUpdate;
    private Item savedItem;
    private Item updatedItem;
    private Comment commentToSave;
    private Comment savedComment;
    private User user;
    private List<Item> savedItems;
    private Booking lastBooking;
    private Booking nextBooking;
    private List<Booking> lastBookings;
    private List<Booking> nextBookings;

    @BeforeEach
    public void init() {
        user = User.builder()
                .id(1L)
                .build();

        itemToSave = Item.builder()
                .name("name")
                .description("description")
                .ownerId(user.getId())
                .isAvailable(true)
                .build();

        savedItem = Item.builder()
                .id(1L)
                .name(itemToSave.getName())
                .description(itemToSave.getDescription())
                .ownerId(itemToSave.getOwnerId())
                .build();

        itemToUpdate = Item.builder()
                .id(savedItem.getId())
                .ownerId(user.getId())
                .build();

        updatedItem = Item.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .ownerId(savedItem.getOwnerId())
                .build();

        savedItems = new ArrayList<>();
        savedItems.add(savedItem);

        lastBooking = Booking.builder()
                .id(1L)
                .item(savedItem)
                .booker(user)
                .build();
        nextBooking = Booking.builder()
                .id(2L)
                .item(savedItem)
                .booker(user)
                .build();

        lastBookings = new ArrayList<>();
        nextBookings = new ArrayList<>();
        lastBookings.add(lastBooking);
        nextBookings.add(nextBooking);

        commentToSave = Comment.builder()
                .text("comment")
                .author(user)
                .item(savedItem)
                .build();

        savedComment = Comment.builder()
                .id(1L)
                .text(commentToSave.getText())
                .item(commentToSave.getItem())
                .author(commentToSave.getAuthor())
                .created(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }

    @Test
    public void addItem_Normal() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.save(itemToSave)).thenReturn(savedItem);

        Item savedItem = itemService.addItem(itemToSave);
        assertEquals(this.savedItem, savedItem);
        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository, times(1)).findById(itemToSave.getOwnerId());
        inOrder.verify(itemRepository, times(1)).save(itemToSave);
    }

    @Test
    public void addItem_WrongUserId() {
        Long wrongUserId = 999999L;
        itemToSave.setOwnerId(wrongUserId);
        when(userRepository.findById(itemToSave.getOwnerId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(ItemNotFoundException.class, () -> itemService.addItem(itemToSave));
        assertEquals(String.format("User id %s not found", itemToSave.getOwnerId()), e.getMessage());
        verify(userRepository, times(1)).findById(itemToSave.getOwnerId());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void updateItem_updateNameNormal() {
        itemToUpdate.setName("updated name");
        updatedItem.setName(itemToUpdate.getName());

        when(itemRepository.findById(itemToUpdate.getId())).thenReturn(Optional.of(savedItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item updatedItem = itemService.updateItem(itemToUpdate);
        assertEquals(savedItem, updatedItem);
        verify(itemRepository, times(1)).findById(itemToUpdate.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void updateItem_updateDescriptionNormal() {
        itemToUpdate.setDescription("updated description");
        updatedItem.setDescription(itemToUpdate.getDescription());

        when(itemRepository.findById(itemToUpdate.getId())).thenReturn(Optional.of(savedItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item updatedItem = itemService.updateItem(itemToUpdate);
        assertEquals(savedItem, updatedItem);
        verify(itemRepository, times(1)).findById(itemToUpdate.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void updateItem_updateAvailableNormal() {
        itemToUpdate.setIsAvailable(false);
        updatedItem.setIsAvailable(itemToUpdate.getIsAvailable());

        when(itemRepository.findById(itemToUpdate.getId())).thenReturn(Optional.of(savedItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item updatedItem = itemService.updateItem(itemToUpdate);
        assertEquals(savedItem, updatedItem);
        verify(itemRepository, times(1)).findById(itemToUpdate.getId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void updateItem_WrongItemId() {
        itemToUpdate.setIsAvailable(false);
        itemToUpdate.setId(9999L);

        when(itemRepository.findById(itemToUpdate.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(itemToUpdate));
        assertEquals(String.format("Item id %s not found", itemToUpdate.getId()), e.getMessage());
        verify(itemRepository, times(1)).findById(itemToUpdate.getId());
        verify(itemRepository, times(0)).save(any(Item.class));
    }

    @Test
    public void updateItem_NotOwner() {
        itemToUpdate.setIsAvailable(false);
        itemToUpdate.setOwnerId(9999L);

        when(itemRepository.findById(itemToUpdate.getId())).thenReturn(Optional.of(savedItem));

        Throwable e = assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(itemToUpdate));
        assertEquals(String.format("Item id %s not found", itemToUpdate.getId()), e.getMessage());
        verify(itemRepository, times(1)).findById(itemToUpdate.getId());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void getAllItems_OwnerNormal() {
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(savedItems));
        when(bookingRepository.findLastBooking(anyLong(), any(Timestamp.class)))
                .thenReturn(lastBookings);
        when(bookingRepository.findNextBooking(anyLong(), any(Timestamp.class)))
                .thenReturn(nextBookings);
        int from = 0;
        int size = 10;

        List<Item> result = itemService.getAllItems(user.getId(), from, size);
        assertEquals(savedItems, result);
    }

    @Test
    public void getAllItems_Normal() {
        Long userIdNull = null;
        when(itemRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(savedItems));
        int from = 0;
        int size = 10;

        List<Item> result = itemService.getAllItems(userIdNull, from, size);
        assertEquals(savedItems, result);
        verify(itemRepository, times(1)).findAll(any(PageRequest.class));
        verify(itemRepository, never()).findAllByOwnerIdOrderById(anyLong(), any(PageRequest.class));
        verify(bookingRepository, never()).findLastBooking(anyLong(), any(Timestamp.class));
        verify(bookingRepository, never()).findNextBooking(anyLong(), any(Timestamp.class));
    }

    @Test
    public void getAllItems_OwnerNoBookingsNormal() {
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(savedItems));
        int from = 0;
        int size = 10;

        List<Item> result = itemService.getAllItems(user.getId(), from, size);
        assertEquals(savedItems, result);
    }

    @Test
    public void getItem_ByOwnerNormal() {
        when(itemRepository.findById(savedItem.getId())).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findLastBooking(anyLong(), any(Timestamp.class)))
                .thenReturn(lastBookings);
        when(bookingRepository.findNextBooking(anyLong(), any(Timestamp.class)))
                .thenReturn(nextBookings);

        Item actualItem = itemService.getItem(savedItem.getId(), savedItem.getOwnerId());
        assertEquals(savedItem, actualItem);
        verify(bookingRepository, times(1)).findLastBooking(anyLong(), any(Timestamp.class));
        verify(bookingRepository, times(1)).findNextBooking(anyLong(), any(Timestamp.class));
    }

    @Test
    public void getItem_NotOwnerNormal() {
        when(itemRepository.findById(savedItem.getId())).thenReturn(Optional.of(savedItem));

        Item actualItem = itemService.getItem(savedItem.getId(), savedItem.getOwnerId() + 999);
        assertEquals(savedItem, actualItem);
        verify(bookingRepository, never()).findLastBooking(anyLong(), any(Timestamp.class));
        verify(bookingRepository, never()).findNextBooking(anyLong(), any(Timestamp.class));
    }

    @Test
    public void getItem_NoSuchItem() {
        when(itemRepository.findById(savedItem.getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(ItemNotFoundException.class, () ->
                itemService.getItem(savedItem.getId(), savedItem.getOwnerId()));

        assertEquals(String.format("Item id %s not found", savedItem.getId()), e.getMessage());
        verify(bookingRepository, never()).findLastBooking(anyLong(), any(Timestamp.class));
        verify(bookingRepository, never()).findNextBooking(anyLong(), any(Timestamp.class));
    }

    @Test
    public void searchItem_Normal() {
        when(itemRepository
                .findItemByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailableIsTrue(any(), any(), any()))
                .thenReturn(new PageImpl<>(savedItems));
        when(bookingRepository.findLastBooking(anyLong(), any(Timestamp.class)))
                .thenReturn(lastBookings);
        when(bookingRepository.findNextBooking(anyLong(), any(Timestamp.class)))
                .thenReturn(nextBookings);
        int from = 0;
        int size = 10;
        String text = "text";

        List<Item> result = itemService.searchItem(text, user.getId(), from, size);
        assertEquals(savedItems, result);
    }

    @Test
    public void searchItem_EmptyTextNormal() {
        String text = "";
        int from = 0;
        int size = 10;

        List<Item> result = itemService.searchItem(text, user.getId(), from, size);
        assertTrue(result.isEmpty());

        text = "   ";

        result = itemService.searchItem(text, user.getId(), from, size);
        assertTrue(result.isEmpty());

        text = null;

        result = itemService.searchItem(text, user.getId(), from, size);
        assertTrue(result.isEmpty());

        verify(itemRepository, never())
                .findItemByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndIsAvailableIsTrue(anyString(),
                        anyString(), any(PageRequest.class));
    }

    @Test
    public void deleteItem_Normal() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(savedItem));

        itemService.deleteItem(savedItem.getId(), savedItem.getOwnerId());

        verify(itemRepository, times(1)).deleteById(savedItem.getId());
    }

    @Test
    public void deleteItem_NoSuchItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        Throwable e = assertThrows(ItemNotFoundException.class, () ->
                itemService.deleteItem(savedItem.getId(), savedItem.getOwnerId()));

        assertEquals(String.format("Item id %s not found", savedItem.getId()), e.getMessage());
        verify(itemRepository, never()).deleteById(savedItem.getId());
    }

    @Test
    public void deleteItem_NotAuthorized() {
        Long wrongUserId = 999999L;
        when(itemRepository.findById(1L)).thenReturn(Optional.of(savedItem));

        Throwable e = assertThrows(ItemNotFoundException.class, () ->
                itemService.deleteItem(savedItem.getId(), wrongUserId));

        assertEquals(String.format("Item id %s not found", savedItem.getId()), e.getMessage());
        verify(itemRepository, never()).deleteById(savedItem.getId());
    }

    @Test
    public void addComment_Normal() {
        when(itemRepository.findById(commentToSave.getItem().getId()))
                .thenReturn(Optional.of(savedItem));
        when(userRepository.findById(commentToSave.getAuthor().getId()))
                .thenReturn(Optional.of(user));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(lastBooking);
        bookings.add(nextBooking);

        when(bookingRepository.findByItemIdAndBookerIdAndStatusNotAndStartDateBefore(anyLong(),
                anyLong(), any(Status.class), any(Timestamp.class))).thenReturn(bookings);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Comment result = itemService.addComment(commentToSave);
        assertEquals(savedComment, result);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void addComment_noSuchItem() {
        when(itemRepository.findById(commentToSave.getItem().getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(ItemNotFoundException.class, () ->
                itemService.addComment(commentToSave));

        assertEquals(String.format("Item id %s not found", commentToSave.getItem().getId()), e.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never())
                .findByItemIdAndBookerIdAndStatusNotAndStartDateBefore(anyLong(), anyLong(),
                        any(Status.class), any(Timestamp.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void addComment_noSuchUser() {
        when(itemRepository.findById(commentToSave.getItem().getId())).thenReturn(Optional.of(savedItem));
        when(userRepository.findById(commentToSave.getAuthor().getId())).thenReturn(Optional.empty());

        Throwable e = assertThrows(UserNotFoundException.class, () ->
                itemService.addComment(commentToSave));

        assertEquals(String.format("User id %s not found", commentToSave.getAuthor().getId()), e.getMessage());
        verify(bookingRepository, never())
                .findByItemIdAndBookerIdAndStatusNotAndStartDateBefore(anyLong(), anyLong(),
                        any(Status.class), any(Timestamp.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void addComment_userDidNotBook() {
        when(itemRepository.findById(commentToSave.getItem().getId())).thenReturn(Optional.of(savedItem));
        when(userRepository.findById(commentToSave.getAuthor().getId())).thenReturn(Optional.of(user));

        Throwable e = assertThrows(CommentNotAllowedException.class, () ->
                itemService.addComment(commentToSave));

        assertEquals(String.format("User id %s did not book item and cannot leave comment",
                commentToSave.getAuthor().getId()), e.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }
}
