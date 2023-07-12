package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.exceptions.RequestNotFoundException;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.request.mapper.RequestMapper.mapFromDto;
import static ru.practicum.shareit.request.mapper.RequestMapper.mapToDto;

@Service
@Slf4j
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public ItemRequestResponseDto addRequest(ItemRequestRequestDto itemRequestRequestDto) {
        log.info("Adding request: {}", itemRequestRequestDto);
        User requester = doesUserExist(itemRequestRequestDto.getRequesterId());
        Request request = mapFromDto(itemRequestRequestDto);
        request.setRequester(requester);
        request.setCreated(Timestamp.valueOf(LocalDateTime.now()));
        Request savedRequest = requestRepository.save(request);
        log.info("Request saved: {}", savedRequest);
        return mapToDto(savedRequest);
    }

    public ItemRequestResponseDto findRequest(Long requestId, Long userId) {
        log.info("Looking for request id {} by user {}", requestId, userId);
        doesUserExist(userId);
        Request request = requestRepository.findById(requestId).orElseThrow(() -> {
            log.error("Request id {} not found.", requestId);
            return new RequestNotFoundException(String.format("Request id %s not found.", requestId));
        });
        log.info("Request found: {}.", request);
        return mapToDto(request);
    }

    public List<ItemRequestResponseDto> findUserRequest(Long userId, int from, int size) {
        log.info("Looking for requests from user id {}. Paging from {}, size {}.", userId, from, size);
        doesUserExist(userId);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Request> requests = requestRepository.findAllByRequesterId(userId, page);
        return requests.map(RequestMapper::mapToDto).getContent();
    }

    public List<ItemRequestResponseDto> findAllRequests(Long userId, int from, int size) {
        log.info("Looking for requests/ Paging from {}, size {}.", from, size);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Request> requests = requestRepository.findAllOrderByCreated(userId, page);
        return requests.map(RequestMapper::mapToDto).getContent();
    }

    private User doesUserExist(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.error("User id {} not found.", id);
            return new UserNotFoundException(String.format("User id %s not found.", id));
        });
    }
}
