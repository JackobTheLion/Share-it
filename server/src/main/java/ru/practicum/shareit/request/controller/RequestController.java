package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/requests")
public class RequestController {
    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestResponseDto addRequest(@RequestBody ItemRequestRequestDto itemRequestRequestDto,
                                             @RequestHeader(value = "X-Sharer-User-Id") Long requesterId) {

        log.info("Request {} received from user id {}", itemRequestRequestDto, requesterId);
        itemRequestRequestDto.setRequesterId(requesterId);
        ItemRequestResponseDto savedRequest = requestService.addRequest(itemRequestRequestDto);
        log.info("Request saved: {}.", savedRequest);
        return savedRequest;
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto findRequest(@PathVariable Long requestId,
                                              @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Looking for request id {} by user {}", requestId, userId);
        ItemRequestResponseDto itemRequestRequestDto = requestService.findRequest(requestId, userId);
        log.info("Request found: {}", itemRequestRequestDto);
        return itemRequestRequestDto;
    }

    @GetMapping
    public List<ItemRequestResponseDto> getOwnRequests(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                       @RequestParam int from,
                                                       @RequestParam int size) {

        log.info("Looking for requests from user id {}. Paging from {}, size {}.", userId, from, size);
        List<ItemRequestResponseDto> requests = requestService.findUserRequest(userId, from, size);
        log.info("Requests found: {}.", requests);
        return requests;
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getRequests(@RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                    @RequestParam int from,
                                                    @RequestParam int size) {

        log.info("Looking for all requests from {}, size {}", from, size);
        List<ItemRequestResponseDto> requests = requestService.findAllRequests(userId, from, size);
        log.info("Requests found: {}", requests);
        return requests;
    }
}