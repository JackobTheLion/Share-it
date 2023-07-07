package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.handler.ErrorHandler;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ItemController.class, ErrorHandler.class})
public class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @SneakyThrows
    @Test
    public void addItem_Normal() {
        Long userId = 1L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        Item savedItem = Item.builder()
                .id(1L)
                .name(itemToSaveDto.getName())
                .description(itemToSaveDto.getDescription())
                .ownerId(userId)
                .build();

        ItemDto savedItemDto = ItemDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .available(savedItem.getIsAvailable())
                .build();

        when(itemService.addItem(any(Item.class))).thenReturn(savedItem);

        String result = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService, times(1)).addItem(any(Item.class));
        assertEquals(objectMapper.writeValueAsString(savedItemDto), result);
    }

    @SneakyThrows
    @Test
    public void addItem_WrongName() {
        Long userId = 1L;
        ItemDto.ItemDtoBuilder builder = ItemDto.builder();
        builder.name("");
        builder.description("description");
        builder.available(true);
        ItemDto itemToSaveDto = builder
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        itemToSaveDto.setName("   ");
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).addItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void addItem_WrongAvailable() {
        Long userId = 1L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).addItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void addItem_WrongDescription() {
        Long userId = 1L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("name")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        itemToSaveDto.setDescription("   ");
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).addItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void addItem_NotAuthorized() {
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("name")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).addItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void addItem_WrongUserId() {
        Long wrongUserId = -99999L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("updated name")
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());
        verify(itemService, times(0)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_UpdateNameNormal() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("new name")
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name(itemToSaveDto.getName())
                .description("description")
                .ownerId(userId)
                .isAvailable(true)
                .build();

        ItemDto updatedItemDto = ItemDto.builder()
                .id(updatedItem.getId())
                .name(updatedItem.getName())
                .description(updatedItem.getDescription())
                .available(updatedItem.getIsAvailable())
                .build();

        when(itemService.updateItem(any(Item.class))).thenReturn(updatedItem);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(updatedItemDto), result);
        verify(itemService, times(1)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_UpdateDescriptionNormal() {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .description("new description")
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("name")
                .description(itemToSaveDto.getDescription())
                .ownerId(userId)
                .isAvailable(true)
                .build();

        ItemDto updatedItemDto = ItemDto.builder()
                .id(updatedItem.getId())
                .name(updatedItem.getName())
                .description(updatedItem.getDescription())
                .available(updatedItem.getIsAvailable())
                .build();

        when(itemService.updateItem(any(Item.class))).thenReturn(updatedItem);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(updatedItemDto), result);
        verify(itemService, times(1)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_NotAuthorized() {
        Long itemId = 1L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("updated name")
                .build();

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_WrongItemId() {
        Long userId = 1L;
        Long wrongItemId = -99999L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("updated name")
                .build();

        mockMvc.perform(patch("/items/{itemId}", wrongItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
        verify(itemService, times(0)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_WrongUserId() {
        Long wrongUserId = -99999L;
        Long itemId = 1L;
        ItemDto itemToSaveDto = ItemDto.builder()
                .name("updated name")
                .build();

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto))
                        .header("X-Sharer-User-Id", wrongUserId))
                .andExpect(status().isBadRequest());
        verify(itemService, times(0)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void getAllItems_Normal() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        List<Item> items = new ArrayList<>();
        Item savedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .build();
        items.add(savedItem);

        List<ItemDto> savedItemsDto = new ArrayList<>();
        ItemDto savedItemDto = ItemDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .build();
        savedItemsDto.add(savedItemDto);

        when(itemService.getAllItems(userId, from, size)).thenReturn(items);

        String result = mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemsDto), result);
        verify(itemService, times(1)).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllItems_UserIdNull() {
        Long userId = null;
        int from = 0;
        int size = 10;
        List<Item> items = new ArrayList<>();
        Item savedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .build();
        items.add(savedItem);

        List<ItemDto> savedItemsDto = new ArrayList<>();
        ItemDto savedItemDto = ItemDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .build();
        savedItemsDto.add(savedItemDto);

        when(itemService.getAllItems(userId, from, size)).thenReturn(items);

        String result = mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemsDto), result);
        verify(itemService, times(1)).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllItems_Empty() {
        Long userId = 1L;
        int from = 0;
        int size = 10;

        List<ItemDto> savedItemsDto = new ArrayList<>();

        when(itemService.getAllItems(userId, from, size)).thenReturn(new ArrayList<>());

        String result = mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemsDto), result);
        verify(itemService, times(1)).getAllItems(userId, from, size);
    }

    @SneakyThrows
    @Test
    public void getItem_Normal() {
        Long userId = 1L;
        Item savedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .isAvailable(true)
                .build();

        ItemDto expectedSavedItem = ItemDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .available(savedItem.getIsAvailable())
                .build();

        when(itemService.getItem(savedItem.getId(), userId)).thenReturn(savedItem);

        String result = mockMvc.perform(get("/items/{itemId}", savedItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedSavedItem), result);
        verify(itemService, times(1)).getItem(savedItem.getId(), userId);
    }

    @SneakyThrows
    @Test
    public void getItem_NoSuchItem() {
        Long userId = 1L;
        Long itemId = 999999L;
        when(itemService.getItem(itemId, userId)).
                thenThrow(new ItemNotFoundException(String.format("Item id %s not found", itemId)));

        String result = mockMvc.perform(get("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(String.format("{\"error\":\"Item id %s not found\"}", itemId), result);
        verify(itemService, times(1)).getItem(itemId, userId);
    }

    @SneakyThrows
    @Test
    public void searchItem_Normal() {
        String text = "name";
        Long userId = 1L;
        int from = 0;
        int size = 10;
        List<Item> items = new ArrayList<>();
        Item savedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .build();
        items.add(savedItem);

        List<ItemDto> savedItemsDto = new ArrayList<>();
        ItemDto savedItemDto = ItemDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .build();
        savedItemsDto.add(savedItemDto);

        when(itemService.searchItem(text, userId, from, size)).thenReturn(items);

        String result = mockMvc.perform(get("/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemsDto), result);
        verify(itemService, times(1)).searchItem(text, userId, from, size);
    }

    @SneakyThrows
    @Test
    public void searchItem_Empty() {
        String text = "name";
        Long userId = 1L;
        int from = 0;
        int size = 10;

        List<ItemDto> savedItemsDto = new ArrayList<>();

        String result = mockMvc.perform(get("/items/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemsDto), result);
        verify(itemService, times(1)).searchItem(text, userId, from, size);
    }

    @SneakyThrows
    @Test
    public void deleteItem_Normal() {
        Long itemId = 1L;
        Long userId = 1L;

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(itemId, userId);
    }

    @SneakyThrows
    @Test
    public void deleteItem_WrongUserId() {
        Long itemId = 1L;
        Long userId = -999L;

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).deleteItem(itemId, userId);
    }

    @SneakyThrows
    @Test
    public void deleteItem_WrongItemId() {
        Long itemId = -999L;
        Long userId = 1L;

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).deleteItem(itemId, userId);
    }

    @SneakyThrows
    @Test
    public void addComment_Normal() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentDto commentToAdd = CommentDto.builder()
                .text("text")
                .build();

        User author = User.builder()
                .id(userId)
                .name("author name")
                .build();

        LocalDateTime now = LocalDateTime.now();

        Comment savedComment = Comment.builder()
                .id(1L)
                .text(commentToAdd.getText())
                .item(new Item())
                .created(Timestamp.valueOf(now))
                .author(author)
                .build();

        CommentDto expectedCommentDto = CommentDto.builder()
                .id(savedComment.getId())
                .text(savedComment.getText())
                .authorName(author.getName())
                .created(now)
                .build();

        when(itemService.addComment(any(Comment.class))).thenReturn(savedComment);

        String result = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentToAdd))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedCommentDto), result);
        verify(itemService, times(1)).addComment(any(Comment.class));
    }


}
