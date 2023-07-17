package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

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
    public ItemResponseDto addItem(@RequestBody ItemRequestDto itemRequestDto,
                                   @RequestHeader(value = "X-Sharer-User-Id") Long userId) {

        log.info("Adding item {} by user {}", itemRequestDto, userId);
        Item item = mapFromDto(itemRequestDto, userId);
        log.info("Item mapped from DTO: {}", item);
        Item savedItem = itemService.addItem(item);
        log.info("Item added: {}", savedItem);
        return mapToDto(savedItem);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(@RequestBody ItemRequestDto itemRequestDto,
                                      @PathVariable Long itemId,
                                      @RequestHeader(value = "X-Sharer-User-Id") Long userId) {

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
                                             @RequestParam int from,
                                             @RequestParam int size) {
        log.info("Getting all items. User id: {}.", userId);
        List<Item> items = itemService.getAllItems(userId, from, size);
        log.info("Number of items found: {}", items.size());
        return items.stream().map(ItemMapper::mapToDto).collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItem(@PathVariable Long itemId,
                                   @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Looking for item id {} by user {}", itemId, userId);
        Item item = itemService.getItem(itemId, userId);
        log.info("Item found: {}", item);
        ItemResponseDto itemResponseDto = mapToDto(item);
        log.info("Item mapped to DTO: {}", itemResponseDto);
        return itemResponseDto;
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItem(@RequestParam String text,
                                            @RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {

        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        List<Item> items = itemService.searchItem(text, userId, from, size);
        log.info("Number of items found: {}", items.size());
        return items.stream().map(ItemMapper::mapToDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public ItemResponseDto deleteItem(@PathVariable Long itemId,
                                      @RequestHeader(value = "X-Sharer-User-Id") Long userId) {

        log.info("Deleting item id {} by user id {}", itemId, userId);
        Item deletedItem = itemService.deleteItem(itemId, userId);
        log.info("Item deleted: {}", deletedItem);
        return mapToDto(deletedItem);
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