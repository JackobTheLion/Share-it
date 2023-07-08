package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private User user;
    private Long wrongUserId = 999999L;

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
        verify(itemRepository, times(0)).save(any(Item.class));
    }
}
