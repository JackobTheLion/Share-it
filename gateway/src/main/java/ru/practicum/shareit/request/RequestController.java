package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import javax.validation.constraints.Min;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestBody @Validated ItemRequestRequestDto itemRequestRequestDto,
                                             @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                                     message = "User id should be more than 0") Long requesterId) {

        log.info("Request {} received from user id {}", itemRequestRequestDto, requesterId);
        ResponseEntity<Object> response = requestClient.addRequest(requesterId, itemRequestRequestDto);
        log.info("Request saved: {}.", response.getBody());
        return response;
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findRequest(@PathVariable Long requestId,
                                              @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                                                      message = "User id should be more than 0") Long userId) {
        log.info("Looking for request id {} by user {}", requestId, userId);
        ResponseEntity<Object> response = requestClient.findRequest(userId, requestId);
        log.info("Request found: {}", response.getBody());
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
            message = "User id should be more than 0") Long userId,
                                                 @RequestParam(defaultValue = "0") @Min(value = 0,
                                                         message = "Parameter 'from' must be more than 0") int from,
                                                 @RequestParam(defaultValue = "10") @Min(value = 0,
                                                         message = "Parameter 'size' must be more than 0") int size) {

        log.info("Looking for requests from user id {}. Paging from {}, size {}.", userId, from, size);
        ResponseEntity<Object> response = requestClient.getOwnRequests(userId, from, size);
        log.info("Requests found: {}.", response.getBody());
        return response;
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequests(
            @RequestHeader(value = "X-Sharer-User-Id") @Min(value = 1,
                    message = "User id should be more than 0") Long userId,
            @RequestParam(defaultValue = "0") @Min(value = 0,
                    message = "Parameter 'from' must be more than 0") int from,
            @RequestParam(defaultValue = "10") @Min(value = 0,
                    message = "Parameter 'size' must be more than 0") int size) {

        log.info("Looking for all requests from {}, size {}", from, size);
        ResponseEntity<Object> response = requestClient.getAllRequests(userId, from, size);
        log.info("Requests found: {}", response.getBody());
        return response;
    }
}
