package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.mapFromDto;
import static ru.practicum.shareit.item.ItemMapper.mapToDto;

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

        //validateUserId(userId);
        log.info("Adding item {} by user {}", itemDto, userId);
        Item item = mapFromDto(itemDto, userId);
        log.info("Item mapped from DTO: {}", item);
        itemService.addItem(item);
        log.info("Item added: {}", item);
        return mapToDto(item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@NotNull @Validated(ValidationGroups.Update.class) @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId,
                              @RequestHeader(value = "X-Sharer-User-Id") Long userId) {

        //validateUserId(userId);
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
    public ItemDto getItem(@PathVariable Long itemId) {
        log.info("Looking for item id {}", itemId);
        Item item = itemService.getItem(itemId);
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
/*
    private void validateUserId(Long userId) throws ValidationException {
        if (userId <= 0) {
            log.error("User id must be more than 0.");
            throw new ValidationException("User id must be more than 0.");
        }
    }*/
}