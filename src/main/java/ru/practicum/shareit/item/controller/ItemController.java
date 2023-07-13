package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.mapFromDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.mapToDto;


@Slf4j
@RestController
@Validated
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemResponseDto addItem(@NotNull @Validated(ValidationGroups.Create.class) @RequestBody ItemRequestDto itemRequestDto,
                                   @RequestHeader(value = "X-Sharer-User-Id")
                                   @Min(value = 1, message = "User ID must be more than 0") Long userId) {

        log.info("Adding item {} by user {}", itemRequestDto, userId);
        Item item = mapFromDto(itemRequestDto, userId);
        log.info("Item mapped from DTO: {}", item);
        Item savedItem = itemService.addItem(item);
        log.info("Item added: {}", savedItem);
        return mapToDto(savedItem);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(@NotNull @Validated(ValidationGroups.Update.class) @RequestBody ItemRequestDto itemRequestDto,
                                      @PathVariable @Min(value = 1, message = "Item ID must be more than 0") Long itemId,
                                      @RequestHeader(value = "X-Sharer-User-Id")
                                      @Min(value = 1, message = "User ID must be more than 0") Long userId) {

        log.info("Updating item id {} as {} by user {}", itemId, itemRequestDto, userId);
        Item item = mapFromDto(itemRequestDto, itemId, userId);
        log.info("Item mapped from DTO: {}", item);
        Item updatedItem = itemService.updateItem(item);
        ItemResponseDto updatedItemResponseDto = mapToDto(updatedItem);
        log.info("Updated item mapped to DTO: {}", updatedItemResponseDto);
        return updatedItemResponseDto;
    }

    @GetMapping
    public List<ItemResponseDto> getAllItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                             @RequestParam(defaultValue = "0") @Min(value = 0,
                                                     message = "Parameter 'from' must be more than 0") int from,
                                             @RequestParam(defaultValue = "10") @Min(value = 0,
                                                     message = "Parameter 'size' must be more than 0") int size) {
        log.info("Getting all items. User id: {}.", userId);
        List<Item> items = itemService.getAllItems(userId, from, size);
        log.info("Number of items found: {}", items.size());
        return items.stream().map(ItemMapper::mapToDto).collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItem(@PathVariable Long itemId,
                                   @RequestHeader(value = "X-Sharer-User-Id", required = false) @Min(value = 1,
                                           message = "User ID must be more than 0") Long userId) {
        log.info("Looking for item id {} by user {}", itemId, userId);
        Item item = itemService.getItem(itemId, userId);
        log.info("Item found: {}", item);
        ItemResponseDto itemResponseDto = mapToDto(item);
        log.info("Item mapped to DTO: {}", itemResponseDto);
        return itemResponseDto;
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItem(@RequestParam String text,
                                            @RequestHeader("X-Sharer-User-Id") @Min(value = 1,
                                                    message = "User ID must be more than 0") Long userId,
                                            @RequestParam(defaultValue = "0") @Min(value = 0,
                                                    message = "Parameter 'from' must be more than 0") int from,
                                            @RequestParam(defaultValue = "10") @Min(value = 0,
                                                    message = "Parameter 'size' must be more than 0") int size) {

        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        List<Item> items = itemService.searchItem(text, userId, from, size);
        log.info("Number of items found: {}", items.size());
        return items.stream().map(ItemMapper::mapToDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable @Min(value = 1, message = "Item ID must be more than 0") Long itemId,
                           @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                   message = "User ID must be more than 0") Long userId) {

        log.info("Deleting item id {} by user id {}", itemId, userId);
        itemService.deleteItem(itemId, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable @Min(value = 1, message = "Item ID must be more than 0") Long itemId,
                                 @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                         message = "User ID must be more than 0") Long userId,
                                 @RequestBody @Validated CommentDto commentDto) {
        log.info("Comment {} from user id {} to item {} received.", commentDto, userId, itemId);
        Comment comment = CommentMapper.mapFromDto(commentDto, userId, itemId);
        Comment savedComment = itemService.addComment(comment);
        log.info("Comment saved: {}", savedComment);
        return CommentMapper.mapToDto(savedComment);
    }
}