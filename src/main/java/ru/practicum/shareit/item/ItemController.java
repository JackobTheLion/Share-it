package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.mapFromDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.mapToDto;


@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@NotNull @Validated(ValidationGroups.Create.class) @RequestBody ItemDto itemDto,
                           @RequestHeader(value = "X-Sharer-User-Id") Long userId) {

        log.info("Adding item {} by user {}", itemDto, userId);
        Item item = mapFromDto(itemDto, userId);
        log.info("Item mapped from DTO: {}", item);
        Item savedItem = itemService.addItem(item);
        log.info("Item added: {}", savedItem);
        return mapToDto(savedItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@NotNull @Validated(ValidationGroups.Update.class) @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId,
                              @RequestHeader(value = "X-Sharer-User-Id") Long userId) {

        log.info("Updating item id {} as {} by user {}", itemId, itemDto, userId);
        Item item = mapFromDto(itemDto, itemId, userId);
        log.info("Item mapped from DTO: {}", item);
        Item updatedItem = itemService.updateItem(item);
        ItemDto updatedItemDto = mapToDto(updatedItem);
        log.info("Updated item mapped to DTO: {}", updatedItemDto);
        return updatedItemDto;
    }

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        if (userId == null) {
            log.info("Getting all items");
        } else {
            log.info("Getting all items of user {}", userId);
        }
        List<Item> items = itemService.getAllItems(userId);
        log.info("Number of items found: {}", items.size());
        return items.stream().map(ItemMapper::mapToDto).collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId,
                           @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Looking for item id {} by user {}", itemId, userId);
        Item item = itemService.getItem(itemId, userId);
        log.info("Item found: {}", item);
        ItemDto itemDto = mapToDto(item);
        log.info("Item mapped to DTO: {}", itemDto);
        return itemDto;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text,
                                    @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        List<Item> items = itemService.searchItem(text, userId);
        log.info("Number of items found: {}", items.size());
        return items.stream().map(ItemMapper::mapToDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId,
                           @RequestHeader(value = "X-Sharer-User-Id") Long userId) {

        log.info("Deleting item id {} by user id {}", itemId, userId);
        itemService.deleteItem(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                 @RequestBody CommentDto commentDto) {
        log.info("Comment {} from user id {} to item {} received.", commentDto, userId, itemId);
        Comment comment = CommentMapper.mapFromDto(commentDto, userId, itemId);
        Comment savedComment = itemService.addComment(comment);
        log.info("Comment saved: {}", savedComment);
        return CommentMapper.mapToDto(savedComment);
    }
}