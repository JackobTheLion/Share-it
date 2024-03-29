package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
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
import ru.practicum.shareit.item.dto.ItemRequestDto;
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

    private ItemRequestDto itemToSaveDto;
    private Item savedItem;
    private ItemRequestDto savedItemRequestDto;
    private Item updatedItem;
    private ItemRequestDto updatedItemRequestDto;
    private List<Item> savedItems;
    private List<ItemRequestDto> savedItemsDto;
    private CommentDto commentToAdd;
    private User author;
    private LocalDateTime now = LocalDateTime.now();
    private Comment savedComment;
    private CommentDto expectedCommentDto;
    private Long userId = 1L;
    private Long itemId = 1L;
    private Long wrongUserId = -99999L;
    private Long wrongItemId = -99999L;
    private int from = 0;
    private int size = 10;
    private String text = "name";


    @BeforeEach
    public void init() {
        itemToSaveDto = ItemRequestDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        savedItem = Item.builder()
                .id(itemId)
                .name(itemToSaveDto.getName())
                .description(itemToSaveDto.getDescription())
                .ownerId(userId)
                .build();

        savedItemRequestDto = ItemRequestDto.builder()
                .id(savedItem.getId())
                .name(savedItem.getName())
                .description(savedItem.getDescription())
                .available(savedItem.getIsAvailable())
                .build();

        updatedItem = Item.builder()
                .id(itemId)
                .name("updated name")
                .description("updated description")
                .ownerId(userId)
                .isAvailable(true)
                .build();

        updatedItemRequestDto = ItemRequestDto.builder()
                .id(updatedItem.getId())
                .name(updatedItem.getName())
                .description(updatedItem.getDescription())
                .available(updatedItem.getIsAvailable())
                .build();

        savedItems = new ArrayList<>();
        savedItems.add(savedItem);

        savedItemsDto = new ArrayList<>();
        savedItemsDto.add(savedItemRequestDto);

        commentToAdd = CommentDto.builder()
                .text("text")
                .build();

        author = User.builder()
                .id(userId)
                .name("author name")
                .build();

        savedComment = Comment.builder()
                .id(1L)
                .text(commentToAdd.getText())
                .item(new Item())
                .created(Timestamp.valueOf(now))
                .author(author)
                .build();

        expectedCommentDto = CommentDto.builder()
                .id(savedComment.getId())
                .text(savedComment.getText())
                .authorName(author.getName())
                .created(now)
                .build();
    }


    @SneakyThrows
    @Test
    public void addItem_Normal() {
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
        assertEquals(objectMapper.writeValueAsString(savedItemRequestDto), result);
    }

    @SneakyThrows
    @Test
    public void addItem_NotAuthorized() {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToSaveDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).addItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_UpdateNameNormal() {
        ItemRequestDto itemToUpdateDto = ItemRequestDto.builder()
                .name("new name")
                .build();

        updatedItem.setName(itemToUpdateDto.getName());
        updatedItemRequestDto.setName(itemToUpdateDto.getName());

        when(itemService.updateItem(any(Item.class))).thenReturn(updatedItem);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToUpdateDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(updatedItemRequestDto), result);
        verify(itemService, times(1)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_UpdateDescriptionNormal() {
        ItemRequestDto itemToUpdateDto = ItemRequestDto.builder()
                .description("new description")
                .build();

        updatedItem.setDescription(itemToUpdateDto.getDescription());
        updatedItemRequestDto.setDescription(itemToUpdateDto.getDescription());

        when(itemService.updateItem(any(Item.class))).thenReturn(updatedItem);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToUpdateDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(updatedItemRequestDto), result);
        verify(itemService, times(1)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void updateItem_NotAuthorized() {
        ItemRequestDto itemToUpdateDto = ItemRequestDto.builder()
                .name("updated name")
                .build();

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToUpdateDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, times(0)).updateItem(any(Item.class));
    }

    @SneakyThrows
    @Test
    public void getAllItems_Normal() {
        when(itemService.getAllItems(userId, from, size)).thenReturn(savedItems);

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
        Long userIdNull = null;

        when(itemService.getAllItems(userIdNull, from, size)).thenReturn(savedItems);

        String result = mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", Integer.toString(from))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(savedItemsDto), result);
        verify(itemService, times(1)).getAllItems(userIdNull, from, size);
    }

    @SneakyThrows
    @Test
    public void getAllItems_Empty() {
        List<ItemRequestDto> savedItemsDto = new ArrayList<>();

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
        ItemRequestDto expectedSavedItem = ItemRequestDto.builder()
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
        Long itemId = 999999L;
        when(itemService.getItem(itemId, userId))
                .thenThrow(new ItemNotFoundException(String.format("Item id %s not found", itemId)));

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
        when(itemService.searchItem(text, userId, from, size)).thenReturn(savedItems);

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
        List<ItemRequestDto> savedItemsDto = new ArrayList<>();

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
        when(itemService.deleteItem(anyLong(), anyLong())).thenReturn(savedItem);

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(itemId, userId);
    }

    @SneakyThrows
    @Test
    public void addComment_Normal() {
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
