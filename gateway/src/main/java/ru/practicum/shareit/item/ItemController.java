package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@NotNull @Validated(ValidationGroups.Create.class) @RequestBody ItemRequestDto itemRequestDto,
                                          @RequestHeader(value = "X-Sharer-User-Id")
                                          @Min(value = 1, message = "User ID must be more than 0") Long userId) {

        log.info("Adding item {} by user {}", itemRequestDto, userId);
        ResponseEntity<Object> response = itemClient.addItem(userId, itemRequestDto);
        log.info("Item added: {}", response.getBody());
        return response;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@NotNull @Validated(ValidationGroups.Update.class) @RequestBody ItemRequestDto itemRequestDto,
                                             @PathVariable @Min(value = 1, message = "Item ID must be more than 0") Long itemId,
                                             @RequestHeader(value = "X-Sharer-User-Id")
                                             @Min(value = 1, message = "User ID must be more than 0") Long userId) {

        log.info("Updating item id {} as {} by user {}", itemId, itemRequestDto, userId);
        ResponseEntity<Object> response = itemClient.updateItem(userId, itemId, itemRequestDto);
        log.info("Updated item mapped to DTO: {}", response.getBody());
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                              @RequestParam(defaultValue = "0") @Min(value = 0,
                                                      message = "Parameter 'from' must be more than 0") int from,
                                              @RequestParam(defaultValue = "10") @Min(value = 0,
                                                      message = "Parameter 'size' must be more than 0") int size) {
        log.info("Getting all items. User id: {}.", userId);
        ResponseEntity<Object> response = itemClient.getAllItems(userId, from, size);
        log.info("Number of items found: {}", response.getBody());
        return response;
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@PathVariable Long itemId,
                                          @RequestHeader(value = "X-Sharer-User-Id", required = false) @Min(value = 1,
                                                  message = "User ID must be more than 0") Long userId) {
        log.info("Looking for item id {} by user {}", itemId, userId);
        ResponseEntity<Object> response = itemClient.getItem(userId, itemId);
        log.info("Item found: {}", response.getBody());
        return response;
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text,
                                             @RequestHeader("X-Sharer-User-Id") @Min(value = 1,
                                                     message = "User ID must be more than 0") Long userId,
                                             @RequestParam(defaultValue = "0") @Min(value = 0,
                                                     message = "Parameter 'from' must be more than 0") int from,
                                             @RequestParam(defaultValue = "10") @Min(value = 0,
                                                     message = "Parameter 'size' must be more than 0") int size) {

        log.info("Looking for item by key word: \"{}\". User id: {}", text, userId);
        ResponseEntity<Object> response = itemClient.searchItem(userId, from, size, text);
        log.info("Items found: {}", response.getBody());
        return response;
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable @Min(value = 1, message = "Item ID must be more than 0") Long itemId,
                                             @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                                     message = "User ID must be more than 0") Long userId) {

        log.info("Deleting item id {} by user id {}", itemId, userId);
        ResponseEntity<Object> response = itemClient.deleteItem(itemId, userId);
        log.info("Item deleted: {}", response.getBody());
        return response;
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable @Min(value = 1, message = "Item ID must be more than 0") Long itemId,
                                             @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                                     message = "User ID must be more than 0") Long userId,
                                             @RequestBody @Validated CommentDto commentDto) {
        log.info("Comment {} from user id {} to item {} received.", commentDto, userId, itemId);
        ResponseEntity<Object> response = itemClient.addComment(userId, itemId, commentDto);
        log.info("Comment saved: {}", response.getBody());
        return response;
    }
}
